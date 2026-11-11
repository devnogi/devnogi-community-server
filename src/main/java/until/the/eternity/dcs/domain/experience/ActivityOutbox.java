package until.the.eternity.dcs.domain.experience;

import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(propagation = Propagation.MANDATORY)
public class ActivityOutbox {
    private final JdbcTemplate db;
    private final boolean captureEnabled;

    public ActivityOutbox(
            JdbcTemplate db, @Value("${experience.capture-enabled:false}") boolean captureEnabled) {
        this.db = db;
        this.captureEnabled = captureEnabled;
    }

    /** Parent locking is independent of the rollout gate so concurrent mutations remain ordered. */
    public void lockPost(long id) {
        if (db.queryForList(
                        "SELECT id FROM post WHERE id=? AND is_deleted=0 AND COALESCE(is_blocked,0)=0 FOR UPDATE",
                        id)
                .isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다.");
        }
    }

    public long lockCommentRoot(long id) {
        List<Long> roots =
                db.query(
                        "SELECT post_id FROM comment WHERE id=?",
                        (r, n) -> (Long) r.getObject(1),
                        id);
        if (roots.isEmpty() || roots.getFirst() == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "댓글을 찾을 수 없습니다.");
        long root = roots.getFirst();
        lockPost(root);
        if (db.queryForList(
                        "SELECT id FROM comment WHERE id=? AND post_id=? AND is_deleted=0 AND COALESCE(is_blocked,0)=0 FOR UPDATE",
                        id,
                        root)
                .isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "댓글을 찾을 수 없습니다.");
        }
        return root;
    }

    public void created(ActivitySource source, long id, long author, long root) {
        changed(source, id, author, author, root, true);
    }

    public void changed(
            ActivitySource source, long id, long actor, long author, long root, boolean active) {
        if (!captureEnabled || (source.activityType.equals("LIKE") && actor == author)) return;
        insert(source, id, actor, author, root, active, Instant.now(), false);
    }

    /** Must run before soft-delete/disconnection while the parent write lock is held. */
    public void deletedRoot(String rootType, long id) {
        if (!captureEnabled) return;
        for (ActivitySource source : ActivitySource.values()) {
            if (source.rootType.equals(rootType)) snapshot(source, "root_id", id);
        }
    }

    public void deletedComment(long id) {
        if (!captureEnabled) return;
        snapshot(ActivitySource.COMMENT, "target_id", id);
        snapshot(ActivitySource.COMMENT_LIKE, "target_id", id);
    }

    private void snapshot(ActivitySource source, String column, long id) {
        db.query(
                "SELECT * FROM (" + source.deletionSelect() + ") activity WHERE " + column + "=?",
                (org.springframework.jdbc.core.RowCallbackHandler)
                        r ->
                                insert(
                                        source,
                                        r.getLong("target_id"),
                                        r.getLong("actor_id"),
                                        r.getLong("author_id"),
                                        r.getLong("root_id"),
                                        false,
                                        Instant.now(),
                                        false),
                id);
    }

    void baseline(
            ActivitySource source,
            long id,
            long actor,
            long author,
            long root,
            Instant occurredAt) {
        insert(source, id, actor, author, root, true, occurredAt, true);
    }

    private void insert(
            ActivitySource source,
            long id,
            long actor,
            long author,
            long root,
            boolean active,
            Instant at,
            boolean baseline) {
        if (source.activityType.equals("LIKE") && actor == author) return;
        String key = source.key(id, actor);
        String event =
                baseline
                        ? UUID.nameUUIDFromBytes(
                                        ("activity-v1:" + key + ":0")
                                                .getBytes(StandardCharsets.UTF_8))
                                .toString()
                        : UUID.randomUUID().toString();
        LocalDateTime enqueuedAt = LocalDateTime.now(java.time.Clock.systemUTC());
        db.update(
                "INSERT INTO activity_outbox(event_id,activity_key,activity_type,actor_user_id,author_user_id,target_type,target_id,root_target_type,root_target_id,active,baseline,occurred_at,next_attempt_at,created_at) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE event_id=event_id",
                event,
                key,
                source.activityType,
                actor,
                author,
                source.targetType,
                id,
                source.rootType,
                root,
                active,
                baseline,
                Timestamp.valueOf(LocalDateTime.ofInstant(at, ZoneOffset.UTC)),
                enqueuedAt,
                enqueuedAt);
    }
}
