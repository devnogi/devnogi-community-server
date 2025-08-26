package until.the.eternity.dcs.domain.notice.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.RedisStreamCommands.XAddOptions;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import until.the.eternity.dcs.domain.notice.dto.stream.NotificationJob;

@Service
@RequiredArgsConstructor
public class RedisSender {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper om = new ObjectMapper();

    @Value("${app.notification.stream}")
    private String stream;

    @Value("${app.notification.maxLen}")
    private long maxLen;

    public RecordId enqueue(NotificationJob job) {
        Map<String, String> f = new HashMap<>();
        f.put("notificationId", job.notificationId());
        f.put("userId", String.valueOf(job.userId()));
        f.put("channel", job.channel());
        f.put("templateCode", job.templateCode());
        f.put("vars", writeJson(job.vars()));

        XAddOptions opts = XAddOptions.maxlen(maxLen).approximateTrimming(true);
        return redisTemplate
                .opsForStream()
                .add(StreamRecords.mapBacked(f).withStreamKey(stream), opts);
    }

    private String writeJson(Object o) {
        try {
            return om.writeValueAsString(o);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }
}
