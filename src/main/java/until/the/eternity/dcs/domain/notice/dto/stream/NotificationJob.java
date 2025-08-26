package until.the.eternity.dcs.domain.notice.dto.stream;

import java.util.Map;

public record NotificationJob(
        String notificationId,
        Long userId,
        String channel,
        String templateCode,
        Map<String, Object> vars) {}
