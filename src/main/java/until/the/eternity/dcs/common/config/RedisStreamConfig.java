package until.the.eternity.dcs.common.config;

import io.lettuce.core.RedisBusyException;
import jakarta.annotation.PostConstruct;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
@RequiredArgsConstructor
public class RedisStreamConfig {
    private final RedisConnectionFactory connectionFactory;
    private final StringRedisTemplate redisTemplate;

    @Value("${app.notification.stream}")
    private String stream;

    @Value("${app.notification.group}")
    private String group;

    @PostConstruct
    public void initStreamAndGroup() {
        // 스트림 없으면 더미 레코드로 생성
        if (!redisTemplate.hasKey(stream)) {
            String dummyRecordKey = "mk";
            String dummyRecordValue = "1";

            redisTemplate
                    .opsForStream()
                    .add(
                            StreamRecords.mapBacked(Map.of(dummyRecordKey, dummyRecordValue))
                                    .withStreamKey(stream));
        }

        try {
            String offset = "0-0";
            redisTemplate.opsForStream().createGroup(stream, ReadOffset.from(offset), group);
        } catch (RedisSystemException e) {
            Throwable root = e.getRootCause();
            if (!(root instanceof RedisBusyException)) {
                throw e;
            }
        }
    }
}
