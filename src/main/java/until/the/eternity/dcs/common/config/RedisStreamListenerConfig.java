package until.the.eternity.dcs.common.config;

import static org.springframework.data.redis.connection.stream.Consumer.from;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.Executor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer.StreamMessageListenerContainerOptions;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import until.the.eternity.dcs.domain.notice.infrastructure.NotificationWorker;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class RedisStreamListenerConfig {

    private final RedisConnectionFactory cf;
    private final NotificationWorker worker;

    @Value("${app.notification.stream}")
    private String stream;

    @Value("${app.notification.group}")
    private String group;

    @Value("${app.notification.pollTimeoutSec}")
    private long pollTimeoutSec;

    @Bean
    public Executor notifConsumerExecutor() {
        ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();
        ex.setCorePoolSize(4);
        ex.setMaxPoolSize(8);
        ex.setQueueCapacity(200);
        ex.setThreadNamePrefix("notif-worker-");
        ex.initialize();
        return ex;
    }

    @Bean
    public StreamMessageListenerContainer<String, MapRecord<String, String, String>>
            streamContainer(Executor notifConsumerExecutor) {

        StreamMessageListenerContainerOptions<String, MapRecord<String, String, String>> options =
                StreamMessageListenerContainerOptions.builder()
                        .batchSize(10)
                        .pollTimeout(Duration.ofSeconds(pollTimeoutSec))
                        .executor(notifConsumerExecutor)
                        .errorHandler(t -> log.error("Stream listener error", t))
                        .build();

        StreamMessageListenerContainer<String, MapRecord<String, String, String>> container =
                StreamMessageListenerContainer.create(cf, options);
        String consumerName = "c-" + UUID.randomUUID();

        container.receive(
                from(group, consumerName),
                StreamOffset.create(stream, ReadOffset.lastConsumed()),
                worker::handle);

        container.start();
        return container;
    }
}
