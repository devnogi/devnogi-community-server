package until.the.eternity.dcs.domain.comment.dto.request;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CommentLikeToggleRequest(
        @Schema(description = "댓글 아이디", example = "1", requiredMode = REQUIRED)
                @NotNull(message = "댓글 아이디는 Null일 수 없습니다.")
                Long commentId,
        @Schema(description = "사용자 ID", example = "1L", requiredMode = REQUIRED)
                @NotBlank(message = "사용자 ID값은 공란일 수 없습니다.")
                Long userId) {}
