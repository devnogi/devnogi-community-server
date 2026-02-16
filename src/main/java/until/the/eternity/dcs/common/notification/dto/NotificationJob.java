package until.the.eternity.dcs.common.notification.dto;

import lombok.Builder;
import until.the.eternity.dcs.domain.notice.enums.NoticeType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
        return NotificationJob.builder()
                .notificationId(map.get("notificationId"))
                .userId(Long.valueOf(map.get("userId")))
                .channel(map.get("channel"))
                .noticeType(NoticeType.fromCode(map.get("noticeType")).orElseThrow())
                .url(map.get("url"))
                .build();
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
