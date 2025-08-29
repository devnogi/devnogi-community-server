package until.the.eternity.dcs.domain.user.dto.response;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import until.the.eternity.dcs.domain.user.entity.UserSummary;
import until.the.eternity.dcs.domain.user.enums.UserGrade;

@Builder
public record UserSummaryDetailResponse(
        @Schema(description = "사용자 ID", example = "1L", requiredMode = REQUIRED) Long userId,
        @Schema(description = "닉네임", example = "bsko") String nickname,
        @Schema(description = "사용자 등급", example = "USER") UserGrade grade) {
    public static UserSummaryDetailResponse from(UserSummary userSummary) {
        return UserSummaryDetailResponse.builder()
                .userId(userSummary.getId())
                .nickname(userSummary.getNickname())
                .grade(userSummary.getGrade())
                .build();
    }
}
