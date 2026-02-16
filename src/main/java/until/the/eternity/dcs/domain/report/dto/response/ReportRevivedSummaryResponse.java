package until.the.eternity.dcs.domain.report.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ReportRevivedSummaryResponse(
        @Schema(description = "신고 ID", example = "1L") @NotNull(message = "신고 ID는 필수입니다") Long Id,
        @Schema(description = "신고 대상 타입", example = "게시글") @NotNull(message = "신고 대상 타입은 필수입니다")
                String targetType,
        @Schema(description = "신고 대상 사용자 ID", example = "1L")
                @NotNull(message = "신고 대상 사용자 ID는 필수입니다")
                Long targetUserId,
        @Schema(description = "신고 대상 사용자 이름", example = "홍길동")
                @NotNull(message = "신고 대상 사용자 이름은 필수입니다")
                String targetUsername,
        @Schema(description = "신고 카테고리 타입", example = "스팸/도배") @Enumerated(EnumType.STRING)
                String categoryCd,
        @Schema(description = "복구 처리일자", example = "2025-07-19")
                @NotNull(message = "복구 처리일자는 필수입니다")
                LocalDateTime revivedAt,
        @Schema(description = "복구 처리자", example = "2L") @NotNull(message = "복구 처리자는 필수입니다")
                Long revivedBy) {}
