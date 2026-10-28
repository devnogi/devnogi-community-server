package until.the.eternity.dcs.domain.announcement.dto.response;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import until.the.eternity.dcs.domain.announcement.entity.Announcement;

@Builder
public record AnnouncementToggleResponse(
        @Schema(description = "공지글 아이디", example = "1", requiredMode = REQUIRED) Long id,
        @Schema(description = "공지글 전체공개 여부", example = "true", requiredMode = REQUIRED)
                Boolean isGlobal) {
    public static AnnouncementToggleResponse from(Announcement announcement) {
        return AnnouncementToggleResponse.builder()
                .id(announcement.getId())
                .isGlobal(announcement.getIsGlobal())
                .build();
    }
}
