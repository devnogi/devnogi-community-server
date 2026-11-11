package until.the.eternity.dcs.domain.experience;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

@Component
@Slf4j
@ConditionalOnProperty(name = "experience.publish-enabled", havingValue = "true")
public class ActivityOutboxPublisher {
    public static final String TOPIC = "USER_ACTIVITY_EVENT";
    private final JdbcTemplate db;
    private final TransactionTemplate transaction;
    private final KafkaTemplate<Object, byte[]> kafka;
    private final ObjectMapper mapper;

    public ActivityOutboxPublisher(
            JdbcTemplate db,
            TransactionTemplate transaction,
            KafkaTemplate<Object, byte[]> kafka,
            ObjectMapper mapper) {
        this.db = db;
        this.transaction = transaction;
        this.kafka = kafka;
        this.mapper = mapper;
    }

    record Claim(long id, String token, int attempts, ActivityEvent event) {}

    @Scheduled(fixedDelayString = "${experience.publish-delay-ms:1000}")
    public void publish() {
        // One leased record at a time: a slow broker must not expire an entire claimed batch.
        for (int count = 0; count < 100; count++) {
            Claim claim = transaction.execute(status -> claim());
            if (claim == null) return;
            try {
                kafka.send(
                                TOPIC,
                                claim.event().activityKey(),
                                mapper.writeValueAsBytes(claim.event()))
                        .get(20, TimeUnit.SECONDS);
                db.update(
                        "UPDATE activity_outbox SET published_at=UTC_TIMESTAMP(6),claim_token=NULL,claimed_until=NULL WHERE id=? AND claim_token=?",
                        claim.id(),
                        claim.token());
            } catch (Exception failure) {
                if (failure instanceof InterruptedException) Thread.currentThread().interrupt();
                long delay = Math.min(300, 1L << Math.min(8, claim.attempts()));
                db.update(
                        "UPDATE activity_outbox SET claim_token=NULL,claimed_until=NULL,next_attempt_at=? WHERE id=? AND claim_token=?",
                        timestamp(Instant.now().plusSeconds(delay)),
                        claim.id(),
                        claim.token());
                log.warn(
                        "Activity outbox publication delayed: id={}, attempts={}, reason={}",
                        claim.id(),
                        claim.attempts(),
                        failureType(failure));
                return;
            }
        }
    }

    static String failureType(Throwable failure) {
        Throwable cause = failure;
        for (int depth = 0;
                depth < 10 && cause.getCause() != null && cause.getCause() != cause;
                depth++) {
            cause = cause.getCause();
        }
        return cause.getClass().getSimpleName();
    }

    Claim claim() {
        List<Claim> rows =
                db.query(
                        "SELECT * FROM activity_outbox WHERE published_at IS NULL AND next_attempt_at<=UTC_TIMESTAMP(6) AND (claimed_until IS NULL OR claimed_until<UTC_TIMESTAMP(6)) ORDER BY id LIMIT 1 FOR UPDATE SKIP LOCKED",
                        (r, n) -> {
                            long id = r.getLong("id");
                            return new Claim(
                                    id,
                                    UUID.randomUUID().toString(),
                                    r.getInt("attempts") + 1,
                                    new ActivityEvent(
                                            r.getString("event_id"),
                                            1,
                                            r.getString("activity_key"),
                                            r.getString("activity_type"),
                                            r.getLong("actor_user_id"),
                                            r.getLong("author_user_id"),
                                            r.getString("target_type"),
                                            r.getLong("target_id"),
                                            r.getString("root_target_type"),
                                            r.getLong("root_target_id"),
                                            r.getBoolean("active"),
                                            r.getBoolean("baseline") ? 0 : id,
                                            r.getTimestamp("occurred_at")
                                                    .toLocalDateTime()
                                                    .toInstant(ZoneOffset.UTC)));
                        });
        if (rows.isEmpty()) return null;
        Claim claim = rows.getFirst();
        db.update(
                "UPDATE activity_outbox SET claim_token=?,claimed_until=?,attempts=attempts+1 WHERE id=?",
                claim.token(),
                timestamp(Instant.now().plusSeconds(120)),
                claim.id());
        return claim;
    }

    private static Timestamp timestamp(Instant instant) {
        return Timestamp.valueOf(LocalDateTime.ofInstant(instant, ZoneOffset.UTC));
    }
}
