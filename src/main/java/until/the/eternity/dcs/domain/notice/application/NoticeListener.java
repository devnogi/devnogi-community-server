package until.the.eternity.dcs.domain.notice.application;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import until.the.eternity.dcs.common.notification.dto.NotificationJob;
import until.the.eternity.dcs.domain.notice.dto.request.NoticeSendRequest;

@Component
@RequiredArgsConstructor
@Slf4j
public class NoticeListener {
    private static final Duration CLAIM_TTL = Duration.ofDays(7);
    private static final DefaultRedisScript<Long> RELEASE_CLAIM =
            new DefaultRedisScript<>(
                    "if redis.call('get', KEYS[1]) == ARGV[1] then "
                            + "return redis.call('del', KEYS[1]) else return 0 end",
                    Long.class);

    private final StringRedisTemplate redisTemplate;
    private final NoticeService noticeService;

    @Value("${app.notification.group}")
    String group;

    public void handle(MapRecord<String, String, String> message) {
        if (Map.of("mk", "1").equals(message.getValue())) {
            ack(message);
            return;
        }

        NotificationJob job;
        try {
            job = NotificationJob.fromMap(message.getValue());
        } catch (IllegalArgumentException e) {
            log.warn(
                    "Invalid notification record id={} reason={}", message.getId(), e.getMessage());
            return;
        }

        String key = "notif:processed:" + job.notificationId();
        String token = UUID.randomUUID().toString();
        Boolean acquired;
        try {
            acquired = redisTemplate.opsForValue().setIfAbsent(key, token, CLAIM_TTL);
            if (acquired == null) {
                throw new IllegalStateException("No claim result");
            }
        } catch (Exception e) {
            log.error(
                    "Notification claim failed id={} reason={}",
                    message.getId(),
                    e.getClass().getSimpleName());
            return;
        }

        if (!acquired) {
            // Preserve existing duplicate handling; a historical key is not proof of a DB commit.
            ack(message);
            return;
        }

        try {
            // The proxied service returns only after its transaction has committed.
            sendNoticeByChannel(job);
        } catch (Exception e) {
            releaseClaim(key, token, message);
            log.error(
                    "Notification processing failed id={} reason={}",
                    message.getId(),
                    e.getClass().getSimpleName());
            return;
        }

        // ACK failure after a commit must not release the successful delivery's claim.
        ack(message);
    }

    private void releaseClaim(String key, String token, MapRecord<String, String, String> message) {
        try {
            redisTemplate.execute(RELEASE_CLAIM, List.of(key), token);
        } catch (Exception e) {
            log.error(
                    "Notification claim release failed id={} reason={}",
                    message.getId(),
                    e.getClass().getSimpleName());
        }
    }

    private void sendNoticeByChannel(NotificationJob job) {
        if (job.channel().equals("email")) {
            // 이메일 전송
        }

        NoticeSendRequest request = NoticeSendRequest.fromNotificationJob(job);
        noticeService.createNotice(request);
    }

    private void ack(MapRecord<String, String, String> message) {
        try {
            redisTemplate
                    .opsForStream()
                    .acknowledge(
                            Objects.requireNonNull(message.getStream()), group, message.getId());
        } catch (Exception e) {
            log.error(
                    "Notification acknowledgement failed id={} reason={}",
                    message.getId(),
                    e.getClass().getSimpleName());
        }
    }
}
