package until.the.eternity.dcs.common.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import io.lettuce.core.RedisCommandExecutionException;
import java.util.concurrent.Executor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStreamCommands;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.test.util.ReflectionTestUtils;

class RedisStreamConfigTest {
    private final StringRedisTemplate redis = mock(StringRedisTemplate.class);
    private final RedisConnection connection = mock(RedisConnection.class);
    private final RedisStreamCommands streams = mock(RedisStreamCommands.class);
    private RedisStreamConfig config;

    @BeforeEach
    void setUp() {
        config = new RedisStreamConfig(redis);
        ReflectionTestUtils.setField(config, "stream", "test-stream");
        ReflectionTestUtils.setField(config, "group", "test-group");
        when(redis.getStringSerializer()).thenReturn(StringRedisSerializer.UTF_8);
        when(connection.streamCommands()).thenReturn(streams);
        when(redis.execute(any(RedisCallback.class)))
                .thenAnswer(
                        invocation ->
                                invocation.<RedisCallback<?>>getArgument(0).doInRedis(connection));
    }

    @Test
    void createsGroupAtBeginningWithMkstreamAndNoDummyWrite() {
        config.initStreamAndGroup();

        verify(streams)
                .xGroupCreate(
                        eq(StringRedisSerializer.UTF_8.serialize("test-stream")),
                        eq("test-group"),
                        eq(ReadOffset.from("0-0")),
                        eq(true));
        verifyNoMoreInteractions(streams);
        verify(redis, never()).opsForStream();
        verify(redis, never()).hasKey(anyString());
    }

    @Test
    void onlyBusyGroupIsAcceptedAsAnExistingGroup() {
        when(streams.xGroupCreate(any(), anyString(), any(), eq(true)))
                .thenThrow(
                        new RedisSystemException(
                                "command failed",
                                new RedisCommandExecutionException(
                                        "BUSYGROUP Consumer Group name already exists")));

        assertThatCode(config::initStreamAndGroup).doesNotThrowAnyException();
    }

    @Test
    void wrongTypeIsNotHidden() {
        RedisSystemException failure =
                new RedisSystemException(
                        "command failed",
                        new RedisCommandExecutionException(
                                "WRONGTYPE Operation against a key holding the wrong kind of value"));
        when(streams.xGroupCreate(any(), anyString(), any(), eq(true))).thenThrow(failure);

        assertThatThrownBy(config::initStreamAndGroup).isSameAs(failure);
    }

    @Test
    void authenticationFailureIsNotHidden() {
        RedisSystemException failure =
                new RedisSystemException(
                        "command failed",
                        new RedisCommandExecutionException("NOAUTH Authentication required"));
        when(streams.xGroupCreate(any(), anyString(), any(), eq(true))).thenThrow(failure);

        assertThatThrownBy(config::initStreamAndGroup).isSameAs(failure);
    }

    @Test
    void connectionFailureIsNotHidden() {
        RedisConnectionFailureException failure =
                new RedisConnectionFailureException("connection unavailable");
        doThrow(failure).when(redis).execute(any(RedisCallback.class));

        assertThatThrownBy(config::initStreamAndGroup).isSameAs(failure);
    }

    @Test
    void listenerBeanExplicitlyDependsOnInitializer() throws Exception {
        DependsOn dependency =
                RedisStreamListenerConfig.class
                        .getMethod("streamContainer", Executor.class)
                        .getAnnotation(DependsOn.class);

        assertThat(dependency).isNotNull();
        assertThat(dependency.value()).contains("redisStreamConfig");
    }
}
