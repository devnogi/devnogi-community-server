package until.the.eternity.dcs.domain.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import until.the.eternity.dcs.domain.user.entity.UserSummary;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Builder
public record UserSummaryDetailResponse(
        @Schema(description = "사용자 ID", example = "1L", requiredMode = REQUIRED) Long userId,
        @Schema(description = "닉네임", example = "bsko") String nickname,
        @Schema(description = "사용자 등급", example = "user") String grade) {
    public static UserSummaryDetailResponse from(UserSummary userSummary) {
        String userGrade = userSummary.getGrade().getCode();
        return UserSummaryDetailResponse.builder()
                .userId(userSummary.getId())
                .nickname(userSummary.getNickname())
                .grade(userGrade)
                .build();
    }
}
