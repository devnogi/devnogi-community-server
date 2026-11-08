package until.the.eternity.dcs.domain.comment.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import until.the.eternity.dcs.common.notification.RedisSender;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommentCreatedNotificationListener {
    private final RedisSender redisSender;

    @Async("commentNotificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void send(CommentCreatedNotification notification) {
        try {
            redisSender.enqueue(notification.job());
        } catch (RuntimeException exception) {
            log.warn(
                    "Failed to enqueue comment notification after commit: notificationId={}",
                    notification.job().notificationId(),
                    exception);
        }
    }
}
