package until.the.eternity.dcs.domain.notice.application;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import until.the.eternity.dcs.domain.notice.dto.request.NoticeSendRequest;
import until.the.eternity.dcs.domain.notice.enums.NoticeType;

@Component
@RequiredArgsConstructor
@Slf4j
public class NoticeListener {
    private final StringRedisTemplate redisTemplate;
    private final NoticeService noticeService;

    @Value("${app.notification.group}")
    String group;

    public void handle(MapRecord<String, String, String> message) {
        Map<String, String> value = message.getValue();

        try {
            if (alreadyProcessed(value.get("notificationId"))) return;

            sendNoticeByChannel(value);
        } catch (Exception e) {
            log.error("Permanent failure id={} -> DLQ", message.getId(), e);
        } finally {
            ack(message);
        }
    }

    private boolean alreadyProcessed(String notificationId) {
        String key = "notif:processed:" + notificationId;
        Boolean set = redisTemplate.opsForValue().setIfAbsent(key, "1", Duration.ofDays(7));
        return Boolean.FALSE.equals(set); // 이미 존재하면 처리된 것
    }

    private void sendNoticeByChannel(Map<String, String> data) {
        if (data.get("channel").equals("email")) {
            // 이메일 전송
        }

        Long userId = Long.parseLong(data.get("userId"));
        NoticeType type = NoticeType.fromCode(data.get("noticeType")).orElseThrow();
        String url = data.get("url");
        NoticeSendRequest request = new NoticeSendRequest(userId, type, url);
        noticeService.createNotice(request);
    }

    private void ack(MapRecord<String, String, String> message) {
        redisTemplate
                .opsForStream()
                .acknowledge(Objects.requireNonNull(message.getStream()), group, message.getId());
    }
}
