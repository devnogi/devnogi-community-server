package until.the.eternity.dcs.domain.comment.dto.request;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record CommentLikeToggleRequest(
        @Schema(description = "댓글 아이디", example = "1", requiredMode = REQUIRED)
                @NotNull(message = "댓글 아이디는 Null일 수 없습니다.")
                Long commentId) {}
