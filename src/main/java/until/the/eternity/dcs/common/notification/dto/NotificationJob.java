package until.the.eternity.dcs.common.notification.dto;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.Builder;
import until.the.eternity.dcs.domain.notice.enums.NoticeType;

@Builder
public record NotificationJob(
        String notificationId, Long userId, String channel, NoticeType noticeType, String url) {

    public Map<String, String> toMap() {
        HashMap<String, String> record = new HashMap<>();

        record.put("notificationId", this.notificationId());
        record.put("userId", String.valueOf(this.userId()));
        record.put("channel", this.channel());
        record.put("noticeType", this.noticeType().getCode());
        record.put("url", this.url());

        return record;
    }

    public static NotificationJob fromMap(Map<String, String> map) {
        if (map == null) {
            throw new IllegalArgumentException("notification fields are required");
        }
        String notificationId = required(map, "notificationId");
        String userIdValue = required(map, "userId");
        Long userId;
        try {
            userId = Long.valueOf(userIdValue);
        } catch (NumberFormatException e) {
            // Never include the received value in a listener diagnostic.
            throw new IllegalArgumentException("userId must be a valid integer");
        }
        String channel = map.get("channel");
        if (channel == null) {
            throw new IllegalArgumentException("channel is required");
        }
        NoticeType noticeType =
                NoticeType.fromCode(required(map, "noticeType"))
                        .orElseThrow(() -> new IllegalArgumentException("noticeType is unknown"));
        return NotificationJob.builder()
                .notificationId(notificationId)
                .userId(userId)
                .channel(channel)
                .noticeType(noticeType)
                .url(required(map, "url"))
                .build();
    }

    private static String required(Map<String, String> map, String field) {
        String value = map.get(field);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value;
    }

    public static NotificationJob of(Long userId, NoticeType noticeType, Long id) {
        return NotificationJob.builder()
                .notificationId(UUID.randomUUID().toString())
                .userId(userId)
                // 추후 이메일이나 FCM 등이 추가되면 설정
                .channel("")
                .noticeType(noticeType)
                .url(urlBuilder(noticeType, id))
                .build();
    }

    private static String urlBuilder(NoticeType noticeType, Long id) {
        return switch (noticeType) {
            case POST_LIKE, POST_COMMENT, POST_BLOCKED, ANNOUNCEMENT, EVENT -> "/api/posts/" + id;
            case COMMENT_LIKE, COMMENT_REPLY, COMMENT_BLOCKED -> "/api/comments/" + id;
            case REPORT_RESULT -> "/api/reports/replied/" + id;
            default -> "NOT_EXISTS";
        };
    }
}
