package until.the.eternity.dcs.domain.user.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import until.the.eternity.dcs.domain.user.dto.response.UserSummaryConsumerDTO;

@Service
@RequiredArgsConstructor
public class KafkaConsumerService {
    private final UserSummaryProjectionService projectionService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "USER_INFO_UPDATE_EVENT", groupId = "devnogi-community-group")
    public void consume(byte[] payload) throws IOException {
        projectionService.apply(objectMapper.readValue(payload, UserSummaryConsumerDTO.class));
    }
}
