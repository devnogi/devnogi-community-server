package until.the.eternity.dcs.common.notification;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import until.the.eternity.dcs.common.config.RedisStreamConfig;

@Tag("redis-integration")
class RedisNotificationIntegrationTest {
    private final Set<String> ownedKeys = new HashSet<>();
    private LettuceConnectionFactory connectionFactory;
    private StringRedisTemplate redis;
    private StreamOperations<String, String, String> streams;
    private String stream;
    private String group;

    @BeforeEach
    void connectToExplicitRedisSeven() {
        String host = System.getenv("TEST_REDIS_HOST");
        String port = System.getenv("TEST_REDIS_PORT");
        assertThat(host).as("TEST_REDIS_HOST is required").isNotBlank();
        assertThat(port).as("TEST_REDIS_PORT is required").isNotBlank();

        connectionFactory =
                new LettuceConnectionFactory(
                        new RedisStandaloneConfiguration(host, Integer.parseInt(port)),
                        LettuceClientConfiguration.builder()
                                .commandTimeout(Duration.ofSeconds(3))
                                .shutdownTimeout(Duration.ZERO)
                                .build());
        connectionFactory.afterPropertiesSet();
        connectionFactory.start();
        redis = new StringRedisTemplate(connectionFactory);
        streams = redis.opsForStream();

        try (var connection = connectionFactory.getConnection()) {
            assertThat(connection.ping()).isEqualTo("PONG");
            String version =
                    connection.serverCommands().info("server").getProperty("redis_version");
            assertThat(version).as("The approved integration target is Redis 7").startsWith("7.");
        }

        String prefix = "community-redis-test:" + UUID.randomUUID();
        stream = prefix + ":stream";
        group = prefix + ":group";
        ownedKeys.add(stream);
    }

    @AfterEach
    void removeOnlyOwnedRedisKeys() {
        try {
            if (redis != null && !ownedKeys.isEmpty()) {
                redis.delete(ownedKeys);
            }
        } finally {
            if (connectionFactory != null) {
                connectionFactory.destroy();
            }
        }
    }

    @Test
    void initializationCreatesCurrentDummyRecordAndIsIdempotent() {
        RedisStreamConfig config = new RedisStreamConfig(redis);
        ReflectionTestUtils.setField(config, "stream", stream);
        ReflectionTestUtils.setField(config, "group", group);

        config.initStreamAndGroup();

        assertThat(streams.size(stream)).isEqualTo(1L);
        assertThat(streams.groups(stream)).hasSize(1);

        var event =
                streams.add(
                        StreamRecords.mapBacked(Map.of("event", "fixture")).withStreamKey(stream));
        var recordsBefore =
                streams.range(stream, org.springframework.data.domain.Range.unbounded());

        config.initStreamAndGroup();

        assertThat(streams.size(stream)).isEqualTo(2L);
        assertThat(streams.groups(stream)).hasSize(1);
        assertThat(streams.range(stream, org.springframework.data.domain.Range.unbounded()))
                .isEqualTo(recordsBefore)
                .anySatisfy(record -> assertThat(record.getId()).isEqualTo(event));
    }
}
