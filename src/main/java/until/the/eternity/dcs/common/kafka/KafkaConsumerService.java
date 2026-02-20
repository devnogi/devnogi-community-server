package until.the.eternity.dcs.common.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class KafkaConsumerService {
    @KafkaListener(topics = "test3", groupId = "devnogi-community-group")
    public void consume(TestDTO message) {
        log.info("컨수머 테스트 시작");
        log.info("🎉 Kafka로부터 받은 메시지: {}", message);
        log.info("컨수머 종료");
    }
}
