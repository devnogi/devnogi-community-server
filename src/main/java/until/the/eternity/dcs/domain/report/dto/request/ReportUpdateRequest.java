package until.the.eternity.dcs.domain.report.dto.request;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReportUpdateRequest(
        @Schema(description = "신고 상태코드", example = "ACCEPT") @NotNull(message = "신고 상태코드는 필수입니다")
                String statusCd,
        @Schema(description = "사용자(신고자) ID", example = "1L", requiredMode = REQUIRED)
                @NotBlank(message = "사용자 ID값은 공란일 수 없습니다.")
                Long userId) {}
