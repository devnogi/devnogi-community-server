package until.the.eternity.dcs.common.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

class CommentNotificationAsyncConfigTest {

    @Test
    void executorUsesDedicatedBoundedPool() {
        ThreadPoolTaskExecutor executor =
                (ThreadPoolTaskExecutor)
                        new CommentNotificationAsyncConfig().commentNotificationExecutor();
        try {
            assertThat(executor.getCorePoolSize()).isEqualTo(1);
            assertThat(executor.getMaxPoolSize()).isEqualTo(2);
            assertThat(executor.getThreadPoolExecutor().getQueue().remainingCapacity())
                    .isEqualTo(100);
            assertThat(executor.getThreadNamePrefix()).isEqualTo("comment-notification-");
        } finally {
            executor.shutdown();
        }
    }
}
