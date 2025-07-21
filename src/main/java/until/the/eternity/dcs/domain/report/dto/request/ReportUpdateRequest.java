package until.the.eternity.dcs.domain.report.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record ReportUpdateRequest(
        @Schema(description = "신고 상태코드", example = "ACCEPT") @NotNull(message = "신고 상태코드는 필수입니다")
                String statusCd) {}
