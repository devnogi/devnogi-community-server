package until.the.eternity.dcs.domain.experience;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ActivityBackfill {
    private final JdbcTemplate db;
    private final ActivityOutbox outbox;
    private final ZoneId legacyZone;
    private final ZoneId guildZone;

    public ActivityBackfill(
            JdbcTemplate db,
            ActivityOutbox outbox,
            @Value("${experience.backfill.legacy-zone:Asia/Seoul}") String legacyZone,
            @Value("${experience.backfill.guild-zone:Asia/Seoul}") String guildZone) {
        this.db = db;
        this.outbox = outbox;
        this.legacyZone = ZoneId.of(legacyZone);
        this.guildZone = ZoneId.of(guildZone);
    }

    public record Progress(
            String source, long cutoffId, long lastId, boolean complete, long remainingValid) {}

    /** Explicit T0 provided by the Auth cohort freeze; never silently move an existing cutoff. */
    @Transactional
    public void freeze(Instant cutoff) {
        if (cutoff.isAfter(Instant.now()))
            throw new IllegalArgumentException("T0 cannot be in the future");
        for (ActivitySource source : ActivitySource.values()) {
            var existing =
                    db.query(
                            "SELECT cutoff_time FROM activity_backfill_checkpoint WHERE source=? FOR UPDATE",
                            (r, n) -> r.getTimestamp(1).toLocalDateTime().toInstant(ZoneOffset.UTC),
                            source.name());
            if (!existing.isEmpty()) {
                if (!existing.getFirst().equals(cutoff))
                    throw new IllegalStateException("Backfill T0 is already frozen");
                continue;
            }
            Long max =
                    db.queryForObject(
                            "SELECT COALESCE(MAX(id),0) FROM " + source.table, Long.class);
            db.update(
                    "INSERT INTO activity_backfill_checkpoint(source,cutoff_id,cutoff_time) VALUES(?,?,?)",
                    source.name(),
                    max,
                    Timestamp.valueOf(LocalDateTime.ofInstant(cutoff, ZoneOffset.UTC)));
        }
    }

    @Transactional(readOnly = true)
    public List<Progress> dryRun() {
        List<Progress> result = new ArrayList<>();
        for (ActivitySource source : ActivitySource.values()) {
            var checkpoints =
                    db.queryForList(
                            "SELECT * FROM activity_backfill_checkpoint WHERE source=?",
                            source.name());
            if (checkpoints.isEmpty())
                throw new IllegalStateException("Freeze T0 and source cutoffs before dry-run/run");
            var row = checkpoints.getFirst();
            long cutoff = ((Number) row.get("cutoff_id")).longValue();
            long last = ((Number) row.get("last_id")).longValue();
            long remaining =
                    db.queryForObject(
                            "SELECT COUNT(*) FROM ("
                                    + source.select
                                    + ") activity WHERE source_id>? AND source_id<=? AND occurred_at<=?",
                            Long.class,
                            last,
                            cutoff,
                            sourceCutoff(source, row.get("cutoff_time")));
            result.add(
                    new Progress(
                            source.name(),
                            cutoff,
                            last,
                            Boolean.TRUE.equals(row.get("complete")),
                            remaining));
        }
        return result;
    }

    private ZoneId sourceZone(ActivitySource source) {
        return switch (source) {
            case OUTFIT, OUTFIT_COMMENT, OUTFIT_LIKE -> ZoneOffset.UTC;
            case GUILD, GUILD_POST, GUILD_RECRUITMENT -> guildZone;
            default -> legacyZone;
        };
    }

    private LocalDateTime sourceCutoff(ActivitySource source, Object storedCutoff) {
        LocalDateTime utc =
                storedCutoff instanceof Timestamp value
                        ? value.toLocalDateTime()
                        : (LocalDateTime) storedCutoff;
        return LocalDateTime.ofInstant(utc.toInstant(ZoneOffset.UTC), sourceZone(source));
    }

    /** Bounded step per source; checkpoint and baseline outbox writes commit together. */
    @Transactional
    public int step(ActivitySource source, int size) {
        if (size < 1 || size > 1000)
            throw new IllegalArgumentException("Batch size must be 1..1000");
        var rows =
                db.queryForList(
                        "SELECT * FROM activity_backfill_checkpoint WHERE source=? FOR UPDATE",
                        source.name());
        if (rows.isEmpty()) throw new IllegalStateException("Freeze cutoffs first");
        var checkpoint = rows.getFirst();
        if (Boolean.TRUE.equals(checkpoint.get("complete"))) return 0;
        long last = ((Number) checkpoint.get("last_id")).longValue();
        long cutoff = ((Number) checkpoint.get("cutoff_id")).longValue();
        // Scan raw IDs, not only valid rows, so long deleted stretches still make bounded progress.
        List<Long> ids =
                db.query(
                        "SELECT id FROM "
                                + source.table
                                + " WHERE id>? AND id<=? ORDER BY id LIMIT ?",
                        (r, n) -> r.getLong(1),
                        last,
                        cutoff,
                        size);
        long next = ids.isEmpty() ? cutoff : ids.getLast();
        int[] inserted = {0};
        db.query(
                "SELECT * FROM ("
                        + source.select
                        + ") activity WHERE source_id>? AND source_id<=? AND occurred_at<=? ORDER BY source_id",
                (org.springframework.jdbc.core.RowCallbackHandler)
                        r -> {
                            outbox.baseline(
                                    source,
                                    r.getLong("target_id"),
                                    r.getLong("actor_id"),
                                    r.getLong("author_id"),
                                    r.getLong("root_id"),
                                    r.getTimestamp("occurred_at")
                                            .toLocalDateTime()
                                            .atZone(sourceZone(source))
                                            .toInstant());
                            inserted[0]++;
                        },
                last,
                next,
                sourceCutoff(source, checkpoint.get("cutoff_time")));
        db.update(
                "UPDATE activity_backfill_checkpoint SET last_id=?,complete=?,updated_at=UTC_TIMESTAMP(6) WHERE source=?",
                next,
                next >= cutoff || ids.size() < size,
                source.name());
        return inserted[0];
    }
}
