package until.the.eternity.dcs.domain.report.dto.response;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record ReportPersistResponse(
        @Schema(description = "신고 아이디", example = "1", requiredMode = REQUIRED) Long id) {
    public static ReportPersistResponse of(Long id) {
        return ReportPersistResponse.builder().id(id).build();
    }
}
