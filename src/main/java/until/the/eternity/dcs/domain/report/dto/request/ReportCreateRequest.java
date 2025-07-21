package until.the.eternity.dcs.domain.report.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import until.the.eternity.dcs.domain.report.enums.ReportCategory;
import until.the.eternity.dcs.domain.report.enums.ReportTargetType;

public record ReportCreateRequest(
        @Schema(description = "신고 대상 타입", example = "게시글") @NotNull(message = "신고 대상 타입은 필수입니다")
                ReportTargetType targetType,
        @Schema(description = "신고 대상 ID", example = "1L") Long targetId,
        @Schema(description = "신고 대상 사용자 ID", example = "1L")
                @NotNull(message = "신고 대상 사용자 ID는 필수입니다")
                Long targetUserId,
        @Schema(description = "신고 카테고리 타입", example = "스팸/도배") @Enumerated(EnumType.STRING)
                ReportCategory categoryCd,
        @NotNull(message = "신고 사유는 필수입니다") String reason) {}
