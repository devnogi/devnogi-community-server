package until.the.eternity.dcs.domain.user.dto.response;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import until.the.eternity.dcs.domain.user.entity.UserSummary;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@class")
public record UserSummaryConsumerDTO(Long id, String nickname, String profileImageUrl) {
    public UserSummary toEntity() {
        return UserSummary.builder()
                .id(this.id())
                .nickname(this.nickname())
                .profileImageUrl(this.profileImageUrl())
                .build();
    }
}
