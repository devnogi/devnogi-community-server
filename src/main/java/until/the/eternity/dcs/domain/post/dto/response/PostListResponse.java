package until.the.eternity.dcs.domain.post.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import until.the.eternity.dcs.domain.post.entity.Post;
import until.the.eternity.dcs.domain.tag.entity.PostTag;
import until.the.eternity.dcs.domain.tag.entity.Tag;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Builder
@Schema(description = "게시글 목록 응답 DTO")
public class PostListResponse {

    @Schema(description = "게시글 ID", example = "1")
    private Long id;

    @Schema(description = "게시판 이름", example = "자유게시판")
    private String boardName;

    @Schema(description = "게시글 제목", example = "안녕하세요, 첫 게시글입니다")
    private String title;

    @Schema(description = "작성자 이름", example = "홍길동")
    private String authorName;

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

    @Schema(description = "내용 미리보기 (100자)", example = "게시글의 내용 중 일부분을 미리보기로 제공합니다...")
    private String contentPreview;

    @Schema(description = "태그 목록", example = "[\"개발\", \"Spring\", \"JPA\"]")
    private List<String> tags;

    @Schema(description = "썸네일 이미지 URL", example = "https://example.com/thumbnail.jpg")
    private String thumbnailUrl;

    public static PostListResponse from(Post post) {
        return PostListResponse.builder()
                .id(post.getId())
                .boardName(post.getBoard().getName())
                .title(post.getTitle())
                .viewCount(post.getViewCount())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .isDraft(post.getIsDraft())
                .isBlocked(post.getIsBlocked())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .contentPreview(extractPreview(post.getContent()))
                .tags(extractTags(post.getPostTags()))
                .build();
    }

    private static String extractPreview(String content) {
        if (content == null) return "";
        return content.length() > 100 ? content.substring(0, 100) + "..." : content;
    }

    private static List<String> extractTags(List<PostTag> postTags) {
        if (postTags == null) return Collections.emptyList();
        return postTags.stream()
                .map(PostTag::getTag)
                .map(Tag::getName)
                .collect(Collectors.toList());
    }
}
