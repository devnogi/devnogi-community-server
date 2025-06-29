package until.the.eternity.dcs.domain.post.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import until.the.eternity.dcs.domain.board.entity.Board;
import until.the.eternity.dcs.domain.post.entity.Post;
import until.the.eternity.dcs.domain.tag.entity.PostTag;
import until.the.eternity.dcs.domain.tag.entity.Tag;
import until.the.eternity.dcs.domain.user.entity.UserSummary;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Builder
@Schema(description = "게시글 상세 조회 응답 DTO")
public class PostDetailResponse {

    @Schema(description = "게시글 ID", example = "1")
    private Long id;

    @Schema(description = "게시판 정보")
    private Board board;

    @Schema(description = "작성자 정보")
    private UserSummary author;

    @Schema(description = "게시글 제목", example = "안녕하세요, 첫 게시글입니다")
    private String title;

    @Schema(description = "게시글 내용", example = "게시글의 전체 내용입니다.")
    private String content;

    @Schema(description = "조회수", example = "150")
    private Integer viewCount;

    @Schema(description = "좋아요 수", example = "25")
    private Integer likeCount;

    @Schema(description = "댓글 수", example = "10")
    private Integer commentCount;

    @Schema(description = "임시저장 여부", example = "false")
    private Boolean isDraft;

    @Schema(description = "차단 여부", example = "false")
    private Boolean isBlocked;

    @Schema(description = "작성일시", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정일시", example = "2024-01-15T14:20:00")
    private LocalDateTime updatedAt;

    @Schema(description = "태그 목록", example = "[\"개발\", \"Spring\", \"JPA\"]")
    private List<String> tags;

//    @Schema(description = "댓글 목록")
//    private List<CommentResponse> comments;

//    @Schema(description = "현재 사용자의 좋아요 여부", example = "true")
//    private Boolean isLikedByCurrentUser;

//    @Schema(description = "이전/다음 게시글 정보")
//    private PostNavigation navigation;

//    @Getter
//    @AllArgsConstructor
//    @Builder
//    @Schema(description = "게시글 네비게이션 정보")
//    public static class PostNavigation {
//        @Schema(description = "이전 게시글")
//        private PostNavigationItem previous;
//
//        @Schema(description = "다음 게시글")
//        private PostNavigationItem next;
//
//        @Getter
//        @AllArgsConstructor
//        @Builder
//        @Schema(description = "네비게이션 아이템")
//        public static class PostNavigationItem {
//            @Schema(description = "게시글 ID", example = "2")
//            private Long id;
//
//            @Schema(description = "게시글 제목", example = "이전/다음 게시글 제목")
//            private String title;
//        }
//    }

    public static PostDetailResponse from(Post post) {
        return PostDetailResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .viewCount(post.getViewCount())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .isDraft(post.getIsDraft())
                .isBlocked(post.getIsBlocked())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .tags(extractTags(post.getPostTags()))
//                .isLikedByCurrentUser(isLikedByCurrentUser)
                .build();
    }

    private static List<String> extractTags(List<PostTag> postTags) {
        if (postTags == null) return Collections.emptyList();
        return postTags.stream()
                .map(PostTag::getTag)
                .map(Tag::getName)
                .collect(Collectors.toList());
    }
}