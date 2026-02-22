package until.the.eternity.dcs.domain.user.application;

import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import until.the.eternity.dcs.domain.user.dto.response.UserSummaryConsumerDTO;
import until.the.eternity.dcs.domain.user.entity.UserSummary;
import until.the.eternity.dcs.domain.user.infrastructure.UserSummaryRepository;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserSummarySyncScheduler {
    private final UserSummaryRepository userSummaryRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String UNPROCESSED_QUEUE_KEY = "usersummary:queue:unprocessed";

    @Scheduled(cron = "0 */1 * * * *")
    @Transactional
    public void syncUserSummaryToDb() {
        Long size = redisTemplate.opsForList().size(UNPROCESSED_QUEUE_KEY);
        if (size == null || size == 0) {
            return;
        }

        List<UserSummaryConsumerDTO> batchData = new ArrayList<>();
        List<String> poppedUserSummary = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            // leftPop을 쓰면 꺼냄과 동시에 큐에서 ID가 지워지므로 동시성 문제에서 비교적 안전합니다.
            String id = (String) redisTemplate.opsForList().leftPop(UNPROCESSED_QUEUE_KEY);
            if (id == null) {
                continue;
            }
            poppedUserSummary.add(id);
            String dataKey = "temp:user:data" + id;
            UserSummaryConsumerDTO data =
                    (UserSummaryConsumerDTO) redisTemplate.opsForValue().get(dataKey);

            if (data != null) {
                batchData.add(data);
            }
        }

        if (!batchData.isEmpty()) {
            try {
                List<UserSummary> entities = convertToEntities(batchData);
                userSummaryRepository.saveAll(entities);
                log.info("{}건의 데이터를 DB에 일괄 저장했습니다.", entities.size());

            } catch (Exception e) {
                log.error("DB 저장 실패! 큐에 다시 복구합니다.", e);
                redisTemplate.opsForList().rightPushAll(UNPROCESSED_QUEUE_KEY, poppedUserSummary);
            }
        }
    }

    private List<UserSummary> convertToEntities(List<UserSummaryConsumerDTO> batchData) {
        return batchData.stream().map(UserSummaryConsumerDTO::toEntity).toList();
    }
}
