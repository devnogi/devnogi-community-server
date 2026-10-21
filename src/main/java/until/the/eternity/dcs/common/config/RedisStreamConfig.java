package until.the.eternity.dcs.common.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
@RequiredArgsConstructor
public class RedisStreamConfig {
    private final StringRedisTemplate redisTemplate;

    @Value("${app.notification.stream}")
    private String stream;

    @Value("${app.notification.group}")
    private String group;

    @PostConstruct
    public void initStreamAndGroup() {
        try {
            redisTemplate.execute(
                    (RedisCallback<String>)
                            connection ->
                                    connection
                                            .streamCommands()
                                            .xGroupCreate(
                                                    redisTemplate
                                                            .getStringSerializer()
                                                            .serialize(stream),
                                                    group,
                                                    ReadOffset.from("0-0"),
                                                    true));
        } catch (DataAccessException e) {
            String error = e.getMostSpecificCause().getMessage();
            if (error == null || !error.startsWith("BUSYGROUP ")) {
                throw e;
            }
        }
    }
}
