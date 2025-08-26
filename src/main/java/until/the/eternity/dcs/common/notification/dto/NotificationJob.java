package until.the.eternity.dcs.common.notification.dto;

import java.util.HashMap;
import java.util.Map;
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
        return NotificationJob.builder()
                .notificationId(map.get("notificationId"))
                .userId(Long.valueOf(map.get("userId")))
                .channel(map.get("channel"))
                .noticeType(NoticeType.fromCode(map.get("noticeType")).orElseThrow())
                .url(map.get("url"))
                .build();
    }
}
