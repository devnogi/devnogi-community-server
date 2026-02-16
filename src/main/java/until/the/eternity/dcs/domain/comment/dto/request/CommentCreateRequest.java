package until.the.eternity.dcs.domain.comment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

public record CommentCreateRequest(
        @Schema(description = "대댓글일 경우 상위 댓글의 아이디", example = "1", requiredMode = NOT_REQUIRED)
                Long parentComment,
        @Schema(description = "댓글 내용", example = "정말 좋은 게시글이네요!!", requiredMode = REQUIRED)
                @NotBlank(message = "댓글 내용은 공란일 수 없습니다.")
                @Size(max = 255, message = "댓글의 길이는 255자 보다 길 수 없습니다.")
                String content) {}
