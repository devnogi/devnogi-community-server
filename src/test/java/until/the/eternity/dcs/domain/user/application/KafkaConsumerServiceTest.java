package until.the.eternity.dcs.domain.user.application;

import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import until.the.eternity.dcs.domain.user.dto.response.UserSummaryConsumerDTO;

@ExtendWith(MockitoExtension.class)
class KafkaConsumerServiceTest {
    @Mock private UserSummaryProjectionService projectionService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private KafkaConsumerService kafkaConsumerService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        kafkaConsumerService = new KafkaConsumerService(projectionService, objectMapper);
    }

    @Test
    void delegatesValidatedProjectionWithoutLoggingPayload() throws Exception {
        var event =
                new UserSummaryConsumerDTO(
                        42L,
                        "name",
                        null,
                        "류트",
                        true,
                        7L,
                        null,
                        com.fasterxml.jackson.databind.node.NullNode.instance);

        kafkaConsumerService.consume(objectMapper.writeValueAsBytes(event));

        verify(projectionService).apply(event);
    }
}
