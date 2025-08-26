package until.the.eternity.dcs.domain.notice.infrastructure;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationWorker {

    private final StringRedisTemplate redisTemplate;

    @Value("${app.notification.group}")
    String group;

    public void handle(MapRecord<String, String, String> message) {
        Map<String, String> v = message.getValue();
        String notifId = v.get("notificationId");
        String channel = v.get("channel");

        try {
            if (alreadyProcessed(notifId)) return;

            sendNoticeByChannel(channel, v);
        } catch (Exception e) {
            log.error("Permanent failure id={} -> DLQ", message.getId(), e);
        } finally {
            ack(message);
        }
    }

    private boolean alreadyProcessed(String notifId) {
        String key = "notif:processed:" + notifId;
        Boolean set = redisTemplate.opsForValue().setIfAbsent(key, "1", Duration.ofDays(7));
        return Boolean.FALSE.equals(set); // 이미 존재하면 처리된 것
    }

    private void sendNoticeByChannel(String channel, Map<String, String> data) {
        // TODO
    }

    private void ack(MapRecord<String, String, String> message) {
        redisTemplate
                .opsForStream()
                .acknowledge(Objects.requireNonNull(message.getStream()), group, message.getId());
    }
}
