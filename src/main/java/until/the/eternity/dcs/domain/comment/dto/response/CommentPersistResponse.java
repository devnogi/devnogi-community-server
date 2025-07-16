package until.the.eternity.dcs.domain.comment.dto.response;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import until.the.eternity.dcs.domain.comment.entity.Comment;

@Builder
public record CommentPersistResponse(
        @Schema(description = "댓글 아이디", example = "1", requiredMode = REQUIRED) Long id) {
    public static CommentPersistResponse from(Comment comment) {
        return new CommentPersistResponse(comment.getId());
    }
}
