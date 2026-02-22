package until.the.eternity.dcs.domain.user.application;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import until.the.eternity.dcs.domain.user.dto.response.UserSummaryConsumerDTO;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaConsumerService {
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String UNPROCESSED_QUEUE_KEY = "usersummary:queue:unprocessed";

    @KafkaListener(topics = "USER_INFO_UPDATE_EVENT", groupId = "devnogi-community-group")
    public void consume(UserSummaryConsumerDTO userInfo) {
        log.info("Kafka로부터 받은 user 데이터: {}", userInfo);
        String uniqueId = String.valueOf(userInfo.id());
        String dataKey = "temp:user:data" + uniqueId;

        redisTemplate.opsForValue().set(dataKey, userInfo, 24, TimeUnit.HOURS);

        redisTemplate.opsForList().rightPush(UNPROCESSED_QUEUE_KEY, uniqueId);

        log.info("Redis 개별 저장 (24h TTL) 및 큐 등록 완료: ID={}", uniqueId);
        log.info("Redis 개별 저장 (24h TTL) 및 큐 등록 완료: data= {}", userInfo);
    }
}
