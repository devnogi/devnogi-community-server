package until.the.eternity.dcs.domain.user.application;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import until.the.eternity.dcs.domain.user.dto.response.UserSummaryConsumerDTO;

@Service
@RequiredArgsConstructor
public class UserSummaryProjectionService {
    private final JdbcTemplate jdbc;
    private final Validator validator;

    @Transactional
    public void apply(UserSummaryConsumerDTO event) {
        var violations = validator.validate(event);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        for (int attempt = 0; attempt < 3; attempt++) {
            int updated = updateExisting(event);
            if (updated == 1) {
                return;
            }

            List<Long> revisions = currentRevisions(event.id());
            if (!revisions.isEmpty()) {
                Long applied = revisions.get(0);
                if (event.revision() == null || (applied != null && applied >= event.revision())) {
                    return;
                }
                continue;
            }

            try {
                jdbc.update(
                        "INSERT INTO user_summary "
                                + "(id, nickname, profile_image_url, server_name, is_verified, "
                                + "last_auth_sync_revision, level, grade, activity_grade, arcana) "
                                + "VALUES (?, ?, ?, ?, ?, ?, 1, 'USER', ?, ?)",
                        event.id(),
                        event.nickname(),
                        event.profileImageUrl(),
                        event.serverName(),
                        event.resolvedVerified(),
                        event.revision(),
                        event.activityGrade(),
                        event.arcana());
                return;
            } catch (DataIntegrityViolationException conflict) {
                // Only a concurrent insert of this ID is retryable. A different user's unique
                // (server_name, nickname) conflict must remain failed for later redelivery.
                if (currentRevisions(event.id()).isEmpty()) {
                    throw conflict;
                }
            }
        }
        throw new IllegalStateException("User summary update remained contested");
    }

    private int updateExisting(UserSummaryConsumerDTO event) {
        if (event.revision() == null) {
            return jdbc.update(
                    "UPDATE user_summary SET nickname = ?, profile_image_url = ?, server_name = ?, "
                            + "is_verified = ?, activity_grade = ?, arcana = CASE WHEN ? THEN ? ELSE arcana END "
                            + "WHERE id = ? AND last_auth_sync_revision IS NULL",
                    event.nickname(),
                    event.profileImageUrl(),
                    event.serverName(),
                    event.resolvedVerified(),
                    event.activityGrade(),
                    event.hasArcana(),
                    event.arcana(),
                    event.id());
        }
        return jdbc.update(
                "UPDATE user_summary SET nickname = ?, profile_image_url = ?, server_name = ?, "
                        + "is_verified = ?, activity_grade = ?, arcana = CASE WHEN ? THEN ? ELSE arcana END, last_auth_sync_revision = ? WHERE id = ? "
                        + "AND (last_auth_sync_revision IS NULL OR last_auth_sync_revision < ?)",
                event.nickname(),
                event.profileImageUrl(),
                event.serverName(),
                event.resolvedVerified(),
                event.activityGrade(),
                event.hasArcana(),
                event.arcana(),
                event.revision(),
                event.id(),
                event.revision());
    }

    private List<Long> currentRevisions(Long id) {
        // A locking read sees the current committed row under MySQL REPEATABLE READ even when a
        // prior plain SELECT in this transaction would still use an older consistent snapshot.
        return jdbc.query(
                "SELECT last_auth_sync_revision FROM user_summary WHERE id = ? FOR UPDATE",
                (rs, row) -> (Long) rs.getObject(1),
                id);
    }
}
