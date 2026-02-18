package until.the.eternity.dcs.domain.user.dto.response;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import until.the.eternity.dcs.domain.user.entity.UserSummary;

@Builder
public record UserSummaryPersistResponse(
        @Schema(description = "사용자 ID", example = "1L", requiredMode = REQUIRED) Long userId) {
    public static UserSummaryPersistResponse from(UserSummary userSummary) {
        return UserSummaryPersistResponse.builder().userId(userSummary.getId()).build();
    }
}
