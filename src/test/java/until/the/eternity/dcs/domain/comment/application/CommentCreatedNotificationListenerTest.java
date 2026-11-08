package until.the.eternity.dcs.domain.comment.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static until.the.eternity.dcs.domain.notice.enums.NoticeType.POST_COMMENT;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import until.the.eternity.dcs.common.notification.RedisSender;
import until.the.eternity.dcs.common.notification.dto.NotificationJob;

class CommentCreatedNotificationListenerTest {

    @Test
    void notificationIsHandledAsynchronouslyAfterCommit() throws NoSuchMethodException {
        var method =
                CommentCreatedNotificationListener.class.getDeclaredMethod(
                        "send", CommentCreatedNotification.class);

        assertThat(method.getAnnotation(Async.class).value())
                .isEqualTo("commentNotificationExecutor");
        assertThat(method.getAnnotation(TransactionalEventListener.class).phase())
                .isEqualTo(TransactionPhase.AFTER_COMMIT);
    }

    @Test
    void redisFailureAfterCommitDoesNotEscapeListener() {
        RedisSender redisSender = mock(RedisSender.class);
        NotificationJob job = NotificationJob.of(1L, POST_COMMENT, 2L);
        doThrow(new RedisConnectionFailureException("redis unavailable"))
                .when(redisSender)
                .enqueue(job);
        CommentCreatedNotificationListener listener =
                new CommentCreatedNotificationListener(redisSender);

        assertThatCode(() -> listener.send(new CommentCreatedNotification(job)))
                .doesNotThrowAnyException();
    }
}
