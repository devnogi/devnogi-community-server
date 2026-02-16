package until.the.eternity.dcs.domain.announcement.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import until.the.eternity.dcs.domain.announcement.entity.Announcement;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Builder
public record AnnouncementPersistResponse(
        @Schema(description = "공지글 아이디", example = "1", requiredMode = REQUIRED) Long id) {
    public static AnnouncementPersistResponse from(Announcement announcement) {
        return AnnouncementPersistResponse.builder().id(announcement.getId()).build();
    }
}
