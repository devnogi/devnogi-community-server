package until.the.eternity.dcs.domain.user.application;

import org.springframework.stereotype.Component;
import until.the.eternity.dcs.domain.user.dto.request.UserSummaryCreateRequest;
import until.the.eternity.dcs.domain.user.dto.response.UserSummaryDetailResponse;
import until.the.eternity.dcs.domain.user.dto.response.UserSummaryPersistResponse;
import until.the.eternity.dcs.domain.user.entity.UserSummary;
import until.the.eternity.dcs.domain.user.enums.UserGrade;
import until.the.eternity.dcs.domain.user.exception.UserGradeNotFoundException;

@Component
public class UserSummaryConverter {
    public UserSummary createUserSummaryToUserSummary(
            UserSummaryCreateRequest userSummaryCreateRequest) {
        UserGrade userGrade =
                UserGrade.fromCode(userSummaryCreateRequest.grade())
                        .orElseThrow(
                                () ->
                                        new UserGradeNotFoundException(
                                                userSummaryCreateRequest.grade()));
        return UserSummary.builder()
                .id(userSummaryCreateRequest.userId())
                .nickname(userSummaryCreateRequest.nickname())
                .level(userSummaryCreateRequest.level())
                .serverName(userSummaryCreateRequest.serverName())
                .grade(userGrade)
                .build();
    }

    public UserSummaryPersistResponse userSummaryToUserSummaryPersistResponse(
            UserSummary userSummary) {
        return UserSummaryPersistResponse.from(userSummary);
    }

    public UserSummaryDetailResponse userSummaryToUserSummaryDetailResponse(
            UserSummary userSummary) {
        return UserSummaryDetailResponse.from(userSummary);
    }
}
