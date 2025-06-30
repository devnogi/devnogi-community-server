package until.the.eternity.dcs.domain.post.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import until.the.eternity.dcs.domain.post.entity.Post;

import java.time.LocalDateTime;


@Builder
public record PostSummaryResponse(
        @Schema(description = "게시글 ID", example = "1")
        Long id,

        @Schema(description = "게시글 제목", example = "게시글 제목입니다.")
        String title,

        @Schema(description = "조회수", example = "10")
        Integer viewCount,

        @Schema(description = "좋아요 수", example = "5")
        Integer likeCount,

        @Schema(description = "댓글 수", example = "3")
        Integer commentCount,

        @Schema(description = "생성일시")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt

) {
        public static PostSummaryResponse from(Post post){
                return PostSummaryResponse.builder()
                        .id(post.getId())
                        .title(post.getTitle())
                        .viewCount(post.getViewCount())
                        .likeCount(post.getLikeCount())
                        .commentCount(post.getCommentCount())
                        .createdAt(post.getCreatedAt())
                        .build();
        }
}
