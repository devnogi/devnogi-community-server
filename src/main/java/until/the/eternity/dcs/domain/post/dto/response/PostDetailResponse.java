package until.the.eternity.dcs.domain.post.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import until.the.eternity.dcs.domain.post.entity.Post;
import until.the.eternity.dcs.domain.tag.entity.PostTag;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record PostDetailResponse(
        @Schema(description = "게시글 ID", example = "1")
        Long id,

        @Schema(description = "게시판 정보")
        Long boardId,

        @Schema(description = "작성자 ID", example = "1")
        Long userId,

        @Schema(description = "게시글 제목", example = "게시글 제목입니다.")
        String title,

        @Schema(description = "게시글 내용", example = "게시글 내용입니다.")
        String content,

        @Schema(description = "조회수", example = "10")
        Integer viewCount,

        @Schema(description = "좋아요 수", example = "5")
        Integer likeCount,

        @Schema(description = "댓글 수", example = "3")
        Integer commentCount,

        @Schema(description = "임시저장 여부", example = "false")
        Boolean isDraft,

        @Schema(description = "차단 여부", example = "false")
        Boolean isBlocked,

        @Schema(description = "좋아요 여부", example = "false")
        Boolean isLiked,

        @Schema(description = "생성일시")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt,

        @Schema(description = "수정일시")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime updatedAt,

        @Schema(description = "태그 목록")
        List<PostTag> tags
) {
    public static PostDetailResponse from(Post post){
        return PostDetailResponse.builder()
                .id(post.getId())
                .boardId(post.getBoard().getId())
                .userId(post.getUserId())
                .title(post.getTitle())
                .content(post.getContent())
                .viewCount(post.getViewCount())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .isDraft(post.getIsDraft())
                .isBlocked(post.getIsBlocked())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .tags(post.getPostTags())
                .build();
    }

}
