package until.the.eternity.dcs.domain.user.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import until.the.eternity.dcs.domain.user.dto.response.UserSummaryConsumerDTO;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserSummaryDltInboxService {
    private static final long CLAIM_LEASE_MS = 60_000L;
    private static final long MAX_BACKOFF_MS = 60 * 60_000L;
    private static final long ALERT_AGE_MS = 5 * 60_000L;
    private final JdbcTemplate jdbc;
    private final ObjectMapper objectMapper;
    private final UserSummaryProjectionService projectionService;

    @Transactional
    public void store(String topic, int partition, long offset, byte[] payload) {
        long now = Clock.systemUTC().millis();
        try {
            jdbc.update(
                    "INSERT INTO user_summary_dlt_inbox "
                            + "(dlt_topic, dlt_partition, dlt_offset, payload, attempts, "
                            + "next_attempt_at_ms, created_at_ms) VALUES (?, ?, ?, ?, 0, ?, ?)",
                    topic,
                    partition,
                    offset,
                    payload == null ? new byte[0] : payload,
                    now,
                    now);
        } catch (DuplicateKeyException alreadyStored) {
            // Redelivery after a committed insert but before offset acknowledgment.
            log.debug("DLT record already stored: {}-{}@{}", topic, partition, offset);
        }
    }

    @Scheduled(fixedDelayString = "${app.user-summary.dlt-retry-delay-ms:5000}")
    public void retryDue() {
        long now = Clock.systemUTC().millis();
        List<Long> ids =
                jdbc.queryForList(
                        "SELECT id FROM user_summary_dlt_inbox WHERE completed_at_ms IS NULL "
                                + "AND next_attempt_at_ms <= ? "
                                + "AND (claim_until_ms IS NULL OR claim_until_ms < ?) "
                                + "ORDER BY next_attempt_at_ms, id LIMIT 25",
                        Long.class,
                        now,
                        now);
        for (Long id : ids) {
            retryOne(id);
        }
    }

    @Scheduled(fixedDelayString = "${app.user-summary.dlt-observation-delay-ms:60000}")
    public void reportPending() {
        PendingStats pending =
                jdbc.queryForObject(
                        "SELECT COUNT(*), MIN(created_at_ms), MAX(attempts) "
                                + "FROM user_summary_dlt_inbox WHERE completed_at_ms IS NULL",
                        (rs, row) ->
                                new PendingStats(
                                        rs.getLong(1), (Long) rs.getObject(2), rs.getInt(3)));
        if (pending == null || pending.count() == 0) {
            return;
        }
        long oldestAgeMs = Math.max(0L, Clock.systemUTC().millis() - pending.oldestAtMs());
        if (oldestAgeMs >= ALERT_AGE_MS || pending.maxAttempts() >= 3) {
            log.error(
                    "USER_SUMMARY_DLT_UNRESOLVED pending={} oldest_age_ms={} max_attempts={}",
                    pending.count(),
                    oldestAgeMs,
                    pending.maxAttempts());
        } else {
            log.warn(
                    "USER_SUMMARY_DLT_PENDING pending={} oldest_age_ms={} max_attempts={}",
                    pending.count(),
                    oldestAgeMs,
                    pending.maxAttempts());
        }
    }

    private void retryOne(long id) {
        long now = Clock.systemUTC().millis();
        String token = UUID.randomUUID().toString();
        int claimed =
                jdbc.update(
                        "UPDATE user_summary_dlt_inbox SET claim_token = ?, claim_until_ms = ? "
                                + "WHERE id = ? AND completed_at_ms IS NULL "
                                + "AND next_attempt_at_ms <= ? "
                                + "AND (claim_until_ms IS NULL OR claim_until_ms < ?)",
                        token,
                        now + CLAIM_LEASE_MS,
                        id,
                        now,
                        now);
        if (claimed == 0) {
            return;
        }

        var entries =
                jdbc.query(
                        "SELECT payload, attempts FROM user_summary_dlt_inbox WHERE id = ? "
                                + "AND claim_token = ?",
                        (rs, row) -> new Entry(rs.getBytes(1), rs.getInt(2)),
                        id,
                        token);
        if (entries.isEmpty()) {
            return;
        }
        Entry entry = entries.get(0);
        try {
            UserSummaryConsumerDTO event =
                    objectMapper.readValue(entry.payload(), UserSummaryConsumerDTO.class);
            projectionService.apply(event);
            jdbc.update(
                    "UPDATE user_summary_dlt_inbox SET completed_at_ms = ?, "
                            + "claim_token = NULL, claim_until_ms = NULL, last_error = NULL "
                            + "WHERE id = ? AND claim_token = ?",
                    Clock.systemUTC().millis(),
                    id,
                    token);
        } catch (Exception failure) {
            int attempts = entry.attempts() + 1;
            long backoff = Math.min(MAX_BACKOFF_MS, 5_000L * (1L << Math.min(attempts - 1, 10)));
            jdbc.update(
                    "UPDATE user_summary_dlt_inbox SET attempts = ?, next_attempt_at_ms = ?, "
                            + "claim_token = NULL, claim_until_ms = NULL, last_error = ? "
                            + "WHERE id = ? AND claim_token = ?",
                    attempts,
                    Clock.systemUTC().millis() + backoff,
                    failure.getClass().getSimpleName(),
                    id,
                    token);
            log.warn(
                    "User summary DLT record {} remains pending after {} attempts ({})",
                    id,
                    attempts,
                    failure.getClass().getSimpleName());
        }
    }

    private record Entry(byte[] payload, int attempts) {}

    private record PendingStats(long count, Long oldestAtMs, int maxAttempts) {}
}
