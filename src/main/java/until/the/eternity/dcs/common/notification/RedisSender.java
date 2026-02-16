package until.the.eternity.dcs.common.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.RedisStreamCommands.XAddOptions;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import until.the.eternity.dcs.common.notification.dto.NotificationJob;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class RedisSender {
    private final StringRedisTemplate redisTemplate;

    @Value("${app.notification.stream}")
    private String stream;

    @Value("${app.notification.maxLen}")
    private long maxLen;

    public RecordId enqueue(NotificationJob job) {
        Map<String, String> record = job.toMap();

        XAddOptions opts = XAddOptions.maxlen(maxLen).approximateTrimming(true);

        return redisTemplate
                .opsForStream()
                .add(StreamRecords.mapBacked(record).withStreamKey(stream), opts);
    }
}
