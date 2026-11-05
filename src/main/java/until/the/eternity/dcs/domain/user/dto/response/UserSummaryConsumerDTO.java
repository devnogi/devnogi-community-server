package until.the.eternity.dcs.domain.user.dto.response;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import until.the.eternity.dcs.domain.user.entity.UserSummary;

public record UserSummaryConsumerDTO(
        @NotNull(message = "사용자 ID는 필수입니다.") Long id,
        @NotBlank(message = "닉네임은 비어있을 수 없습니다.") String nickname,
        String profileImageUrl,
        @Pattern(regexp = "(?s).*\\P{javaWhitespace}.*", message = "서버명은 비어있을 수 없습니다.")
                String serverName,
        Boolean verified,
        @Positive Long revision) {
    public UserSummaryConsumerDTO(
            Long id, String nickname, String profileImageUrl, String serverName) {
        this(id, nickname, profileImageUrl, serverName, null, null);
    }

    public UserSummaryConsumerDTO(
            Long id, String nickname, String profileImageUrl, String serverName, Boolean verified) {
        this(id, nickname, profileImageUrl, serverName, verified, null);
    }

    public boolean resolvedVerified() {
        return this.verified() != null
                ? this.verified()
                : this.serverName() != null && !this.serverName().isBlank();
    }

    public UserSummary toEntity() {
        return UserSummary.builder()
                .id(this.id())
                .nickname(this.nickname())
                .profileImageUrl(this.profileImageUrl())
                .serverName(this.serverName())
                .verified(resolvedVerified())
                .lastAuthSyncRevision(this.revision())
                .build();
    }
}
