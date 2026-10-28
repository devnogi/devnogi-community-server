package until.the.eternity.dcs.domain.report.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record ReportCreateRequest(
        @Schema(description = "신고 대상 타입", example = "POST") @NotNull(message = "신고 대상 타입은 필수입니다")
                String targetType,
        @Schema(description = "신고 대상 ID", example = "1L") Long targetId,
        @Schema(description = "신고 대상 사용자 ID", example = "1L")
                @NotNull(message = "신고 대상 사용자 ID는 필수입니다")
                Long targetUserId,
        @Schema(description = "신고 카테고리 타입", example = "SPAM") String categoryCd,
        @NotNull(message = "신고 사유는 필수입니다") String reason) {}
