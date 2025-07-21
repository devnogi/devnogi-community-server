package until.the.eternity.dcs.domain.report.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import until.the.eternity.dcs.domain.report.enums.ReportStatus;

public record ReportRepliedUpdateRequest(
        @Schema(description = "신고 ID", example = "1L") @NotNull(message = "신고 ID는 필수입니다") Long Id,
        @Schema(description = "신고 상태코드", example = "1L") @NotNull(message = "신고 상태코드는 필수입니다")
                ReportStatus statusCd,
        @Schema(description = "신고 처리일자", example = "2025-07-19")
                @NotNull(message = "신고 처리일자는 필수입니다")
                LocalDateTime repliedAt,
        @Schema(description = "신고 처리자", example = "2L") @NotNull(message = "신고 처리자는 필수입니다")
                Long repliedBy) {}
