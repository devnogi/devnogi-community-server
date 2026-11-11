package until.the.eternity.dcs.domain.user.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.AssertTrue;
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
        @Positive Long revision,
        @Pattern(regexp = "APPRENTICE|SENIOR|EXPERT|CHAMPION|MASTER|GRANDMASTER")
                String activityGrade,
        @JsonProperty("arcana") JsonNode arcanaValue) {
    public UserSummaryConsumerDTO(
            Long id,
            String nickname,
            String profileImageUrl,
            String serverName,
            Boolean verified,
            Long revision,
            String activityGrade) {
        this(id, nickname, profileImageUrl, serverName, verified, revision, activityGrade, null);
    }

    public String arcana() {
        return arcanaValue == null || arcanaValue.isNull() ? null : arcanaValue.textValue();
    }

    public boolean hasArcana() {
        return arcanaValue != null;
    }

    @AssertTrue(message = "Invalid arcana")
    @JsonIgnore
    public boolean isArcanaValid() {
        return arcanaValue == null
                || arcanaValue.isNull()
                || (arcanaValue.isTextual()
                        && java.util.Set.of(
                                        "ELEMENTAL_KNIGHT",
                                        "SAINT_BARD",
                                        "DARK_MAGE",
                                        "ALCHEMIC_STINGER",
                                        "SACRED_GUARD",
                                        "BLAST_LANCER",
                                        "VARIABLE_GUNNER",
                                        "FORBIDDEN_ALCHEMIST",
                                        "MELODIC_PUPPETEER",
                                        "FURY_FIGHTER")
                                .contains(arcanaValue.textValue()));
    }

    public UserSummaryConsumerDTO(
            Long id,
            String nickname,
            String profileImageUrl,
            String serverName,
            Boolean verified,
            Long revision) {
        this(id, nickname, profileImageUrl, serverName, verified, revision, null);
    }

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
                .activityGrade(this.activityGrade())
                .arcana(this.arcana())
                .build();
    }
}
