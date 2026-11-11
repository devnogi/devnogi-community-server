package until.the.eternity.dcs.domain.experience;

import java.time.Instant;

/** Producer contract. XP amounts and recipient eligibility are exclusively owned by Auth. */
public record ActivityEvent(
        String eventId,
        int schemaVersion,
        String activityKey,
        String activityType,
        long actorUserId,
        long authorUserId,
        String targetType,
        long targetId,
        String rootTargetType,
        long rootTargetId,
        boolean active,
        long sourceRevision,
        Instant occurredAt) {}
