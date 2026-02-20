package until.the.eternity.dcs.common.kafka;

import static org.springframework.http.HttpStatus.NO_CONTENT;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/kafka")
@Slf4j
public class KafkaTestController {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaTestController(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @PostMapping("/test/send")
    public ResponseEntity<Void> sendMessage(@RequestBody TestDTO msg) {
        log.info("kafka controller 테스트 시작: {}", msg);
        kafkaTemplate.send("test-topic", msg);
        log.info("kafka controller 테스트 끝");
        return ResponseEntity.status(NO_CONTENT).build();
    }
}
