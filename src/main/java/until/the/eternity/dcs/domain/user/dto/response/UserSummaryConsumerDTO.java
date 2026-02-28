package until.the.eternity.dcs.domain.user.dto.response;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import until.the.eternity.dcs.domain.user.entity.UserSummary;

public record UserSummaryConsumerDTO(
        @NotNull(message = "사용자 ID는 필수입니다.") Long id,
        @NotBlank(message = "닉네임은 비어있을 수 없습니다.") String nickname,
        String profileImageUrl) {
    public UserSummary toEntity() {
        return UserSummary.builder()
                .id(this.id())
                .nickname(this.nickname())
                .profileImageUrl(this.profileImageUrl())
                .build();
    }
}
