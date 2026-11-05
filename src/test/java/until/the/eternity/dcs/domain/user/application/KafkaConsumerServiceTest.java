package until.the.eternity.dcs.domain.user.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import until.the.eternity.dcs.domain.user.dto.response.UserSummaryConsumerDTO;
import until.the.eternity.dcs.domain.user.entity.UserSummary;
import until.the.eternity.dcs.domain.user.infrastructure.UserSummaryRepository;

@ExtendWith(MockitoExtension.class)
class KafkaConsumerServiceTest {
    @Mock private UserSummaryRepository userSummaryRepository;

    @InjectMocks private KafkaConsumerService kafkaConsumerService;

    @Test
    void promotesExistingUnverifiedUserWhenLegacyEventAddsServerName() {
        kafkaConsumerService.consume(new UserSummaryConsumerDTO(42L, "name", null, "류트", null));

        assertThat(savedUserSummary().isVerified()).isTrue();
    }

    @Test
    void revokesExistingVerifiedUserWhenLegacyEventRemovesServerName() {
        kafkaConsumerService.consume(new UserSummaryConsumerDTO(42L, "name", null, null, null));

        assertThat(savedUserSummary().isVerified()).isFalse();
    }

    @Test
    void appliesExplicitUnverifiedState() {
        kafkaConsumerService.consume(new UserSummaryConsumerDTO(42L, "name", null, null, false));

        assertThat(savedUserSummary().isVerified()).isFalse();
    }

    @Test
    void appliesExplicitVerifiedState() {
        kafkaConsumerService.consume(new UserSummaryConsumerDTO(42L, "name", null, "류트", true));

        assertThat(savedUserSummary().isVerified()).isTrue();
    }

    private UserSummary savedUserSummary() {
        ArgumentCaptor<UserSummary> captor = ArgumentCaptor.forClass(UserSummary.class);
        verify(userSummaryRepository).save(captor.capture());
        return captor.getValue();
    }
}
