package until.the.eternity.dcs.domain.announcement.dto.response;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record AnnouncementPersistResponse(
        @Schema(description = "공지글 아이디", example = "1", requiredMode = REQUIRED) Long id) {}
