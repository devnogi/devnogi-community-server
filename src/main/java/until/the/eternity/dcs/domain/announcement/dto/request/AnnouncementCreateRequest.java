package until.the.eternity.dcs.domain.announcement.dto.request;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record AnnouncementCreateRequest(
        @Schema(description = "공지글 전체공개 여부", example = "true", requiredMode = REQUIRED) @NotNull
                Boolean isGlobal) {}
