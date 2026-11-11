package until.the.eternity.dcs.domain.comment.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import until.the.eternity.dcs.domain.comment.entity.Comment;

public record UserCommentActivityResponse(
        @Schema(description = "댓글 ID", example = "1") Long id,
        @Schema(description = "원문 게시글 ID", example = "10") Long postId,
        @Schema(description = "원문 게시글 제목", example = "게시글 제목입니다.") String postTitle,
        @Schema(description = "댓글 내용", example = "좋은 정보 감사합니다.") String content,
        @Schema(description = "좋아요 수", example = "3") Integer likeCount,
        @Schema(description = "인증 서버명", example = "류트") String serverName,
        @Schema(description = "인게임 인증 여부", example = "true") Boolean verified,
        String activityGrade,
        @Schema(description = "작성일시") @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                LocalDateTime createdAt) {
    public static UserCommentActivityResponse of(
            Comment comment,
            int likeCount,
            String serverName,
            boolean verified,
            String activityGrade) {
        return new UserCommentActivityResponse(
                comment.getId(),
                comment.getPost().getId(),
                comment.getPost().getTitle(),
                comment.getContent(),
                likeCount,
                serverName,
                verified,
                verified ? activityGrade : null,
                comment.getCreatedAt());
    }
}
