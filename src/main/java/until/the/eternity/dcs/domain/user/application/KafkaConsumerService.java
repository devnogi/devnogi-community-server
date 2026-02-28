package until.the.eternity.dcs.domain.user.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import until.the.eternity.dcs.domain.user.dto.response.UserSummaryConsumerDTO;
import until.the.eternity.dcs.domain.user.infrastructure.UserSummaryRepository;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaConsumerService {
    private final UserSummaryRepository userSummaryRepository;

    @KafkaListener(topics = "USER_INFO_UPDATE_EVENT", groupId = "devnogi-community-group")
    public void consume(UserSummaryConsumerDTO userInfo) {
        log.info("Kafka로부터 받은 user 데이터: {}", userInfo);
        userSummaryRepository.save(userInfo.toEntity());
    }
}
