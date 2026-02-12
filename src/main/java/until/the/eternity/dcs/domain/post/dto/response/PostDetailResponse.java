package until.the.eternity.dcs.domain.post.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import until.the.eternity.dcs.domain.post.entity.Post;

@Builder
public record PostDetailResponse(
        @Schema(description = "게시글 ID", example = "1") Long id,
        @Schema(description = "게시판 정보") Long boardId,
        @Schema(description = "작성자 ID", example = "1") Long userId,
        @Schema(description = "작성자 이름", example = "홍길동") String username,
        @Schema(description = "게시글 제목", example = "게시글 제목입니다.") String title,
        @Schema(description = "게시글 내용", example = "게시글 내용입니다.") String content,
        @Schema(description = "조회수", example = "10") Integer viewCount,
        @Schema(description = "좋아요 수", example = "5") Integer likeCount,
        @Schema(description = "댓글 수", example = "3") Integer commentCount,
        @Schema(description = "임시저장 여부", example = "false") Boolean isDraft,
        @Schema(description = "차단 여부", example = "false") Boolean isBlocked,
        @Schema(description = "좋아요 여부", example = "false") Boolean isLiked,
        @Schema(description = "생성일시") @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                LocalDateTime createdAt,
        @Schema(description = "수정일시") @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                LocalDateTime updatedAt,
        @Schema(description = "태그 목록", example = "[\"java\", \"spring\"]") List<String> tags,
        @Schema(description = "이미지 URL 리스트", example = "") List<String> imageUrlList) {
    public static PostDetailResponse from(
            Post post,
            PostMetaResponse postMeta,
            List<String> imageUrlList,
            String username,
            List<String> tags) {
        return PostDetailResponse.builder()
                .id(post.getId())
                .boardId(post.getBoard().getId())
                .userId(post.getUserId())
                .username(username)
                .title(post.getTitle())
                .content(post.getContent())
                .viewCount(postMeta.viewCount())
                .likeCount(postMeta.likeCount())
                .commentCount(postMeta.commentCount())
                .isDraft(post.getIsDraft())
                .isBlocked(post.getIsBlocked())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .tags(tags)
                .imageUrlList(imageUrlList)
                .build();
    }
}
