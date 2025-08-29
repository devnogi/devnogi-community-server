package until.the.eternity.dcs.domain.user.application;

import org.springframework.stereotype.Component;
import until.the.eternity.dcs.domain.user.dto.request.UserSummaryCreateRequest;
import until.the.eternity.dcs.domain.user.dto.response.UserSummaryDetailResponse;
import until.the.eternity.dcs.domain.user.dto.response.UserSummaryPersistResponse;
import until.the.eternity.dcs.domain.user.entity.UserSummary;

@Component
public class UserSummaryConverter {
    public UserSummary createUserSummaryToUserSummary(
            UserSummaryCreateRequest userSummaryCreateRequest) {
        return UserSummary.builder()
                .id(userSummaryCreateRequest.userId())
                .nickname(userSummaryCreateRequest.nickname())
                .level(userSummaryCreateRequest.level())
                .grade(userSummaryCreateRequest.grade())
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
