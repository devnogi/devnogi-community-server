package until.the.eternity.dcs.domain.post.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import until.the.eternity.dcs.domain.post.entity.Post;

import java.util.List;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Builder
public record PostListResponse(
    @Schema(description = "게시글의 개수", example = "1", requiredMode = REQUIRED)
    int count,

    @Schema(description = "게시글 리스트",
            example = """
            [{
                "id": 1,
                "boardId": 1,
                “userId":  1,
                "title": "예시제목",
                "content": "예시내용",
                "viewCount": 1,
                "likeCount": 1,
                "commentCount": 1,
                "isDraft": false,
                “isBlocked”: false,
                "createdAt": "2025-06-30 00:15:00",
                "updatedAt": "2025-06-30 00:17:00",
                “comments”: [COMMENT]
                "tags": [POSTTAG]
            }]
        """,
            requiredMode = REQUIRED)
    List<PostDetailResponse> boards
) {
    public static PostListResponse from(List<Post> postList){
        return PostListResponse.builder()
                .count(postList.size())
                .boards(
                        postList.stream()
                                .map(PostDetailResponse::from)
                                .toList()
                ).build();
    }
}
