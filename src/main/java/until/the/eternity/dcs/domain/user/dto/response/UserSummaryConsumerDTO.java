package until.the.eternity.dcs.domain.user.dto.response;

import until.the.eternity.dcs.domain.user.entity.UserSummary;

public record UserSummaryConsumerDTO(Long id, String nickname, String profileImageUrl) {
    public UserSummary toEntity() {
        return UserSummary.builder()
                .id(this.id())
                .nickname(this.nickname())
                .profileImageUrl(this.profileImageUrl())
                .build();
    }
}
