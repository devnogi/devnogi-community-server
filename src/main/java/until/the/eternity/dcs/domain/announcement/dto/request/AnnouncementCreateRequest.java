package until.the.eternity.dcs.domain.announcement.dto.request;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AnnouncementCreateRequest(
        @Schema(description = "공지글 전체공개 여부", example = "true", requiredMode = REQUIRED) @NotNull
                Boolean isGlobal,
        @Schema(description = "사용자 ID", example = "1L", requiredMode = REQUIRED)
                @NotBlank(message = "사용자 ID값은 공란일 수 없습니다.")
                Long userId) {}
