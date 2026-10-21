package until.the.eternity.dcs.domain.user.application;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import until.the.eternity.dcs.domain.user.dto.response.UserSummaryConsumerDTO;
import until.the.eternity.dcs.domain.user.infrastructure.UserSummaryRepository;

@Service
@RequiredArgsConstructor
public class KafkaConsumerService {
    private final UserSummaryRepository userSummaryRepository;
    private final Validator validator;

    @Transactional
    @KafkaListener(topics = "USER_INFO_UPDATE_EVENT", groupId = "devnogi-community-group")
    public void consume(UserSummaryConsumerDTO userInfo) {
        var violations = validator.validate(userInfo);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        var userSummary =
                userSummaryRepository.findById(userInfo.id()).orElseGet(userInfo::toEntity);
        userSummary.updateFromAuth(
                userInfo.nickname(), userInfo.profileImageUrl(), userInfo.serverName());
        userSummaryRepository.save(userSummary);
    }
}
