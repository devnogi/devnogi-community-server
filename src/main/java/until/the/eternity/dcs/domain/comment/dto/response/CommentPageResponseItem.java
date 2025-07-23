package until.the.eternity.dcs.domain.comment.dto.response;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import until.the.eternity.dcs.domain.comment.entity.Comment;

@Builder
public record CommentPageResponseItem(
        @Schema(description = "댓글 아이디", example = "1", requiredMode = REQUIRED) Long id,
        @Schema(description = "글쓴이 아이디", example = "1", requiredMode = REQUIRED) Long userId,
        @Schema(description = "대댓글일 경우 상위 댓글의 아이디", example = "1", requiredMode = NOT_REQUIRED)
                Long parentComment,
        @Schema(description = "댓글 내용", example = "정말 좋은 게시글이네요!!", requiredMode = REQUIRED)
                String content,
        @Schema(description = "좋아요 수", example = "0", requiredMode = REQUIRED) Integer likeCount,
        @Schema(description = "삭제 여부", example = "false", requiredMode = REQUIRED)
                Boolean isDeleted,
        @Schema(description = "차단 여부", example = "false", requiredMode = REQUIRED)
                Boolean isBlocked,
        @Schema(description = "현재 로그인한 유저의 좋아요 여부", example = "false", requiredMode = REQUIRED)
                Boolean isLiked) {
    public static CommentPageResponseItem from(
            Comment comment, Boolean isLiked, Integer likeCount) {
        if (comment.getParentComment() == null) {
            return CommentPageResponseItem.builder()
                    .id(comment.getId())
                    .userId(comment.getUserId())
                    .content(comment.getContent())
                    .likeCount(likeCount)
                    .isDeleted(comment.getIsDeleted())
                    .isBlocked(comment.getIsBlocked())
                    .isLiked(isLiked)
                    .build();
        }

        return CommentPageResponseItem.builder()
                .id(comment.getId())
                .userId(comment.getUserId())
                .parentComment(comment.getParentComment().getId())
                .content(comment.getContent())
                .likeCount(likeCount)
                .isDeleted(comment.getIsDeleted())
                .isBlocked(comment.getIsBlocked())
                .isLiked(isLiked)
                .build();
    }
}
