package until.the.eternity.dcs.domain.post.application;

import java.util.List;
import org.springframework.stereotype.Component;
import until.the.eternity.dcs.domain.board.entity.Board;
import until.the.eternity.dcs.domain.post.dto.request.PostCreateRequest;
import until.the.eternity.dcs.domain.post.dto.response.PostDetailResponse;
import until.the.eternity.dcs.domain.post.dto.response.PostMetaResponse;
import until.the.eternity.dcs.domain.post.dto.response.PostPersistResponse;
import until.the.eternity.dcs.domain.post.dto.response.PostSummaryResponse;
import until.the.eternity.dcs.domain.post.entity.Post;
import until.the.eternity.dcs.domain.user.entity.UserSummary;

@Component
public class PostConverter {

    public Post fromCreateRequestToPost(PostCreateRequest request, Long userId) {
        Board board = Board.builder().id(request.boardId()).build();
        return Post.builder()
                .board(board)
                .userId(userId)
                .title(request.title())
                .content(request.content())
                .isDraft(request.isDraft())
                .build();
    }

    public PostSummaryResponse fromPostToPostSummaryResponse(
            Post post, PostMetaResponse postMeta, UserSummary userSummary) {
        return PostSummaryResponse.of(post, postMeta, userSummary);
    }

    public PostDetailResponse fromPostToPostDetailResponse(
            Post post,
            PostMetaResponse postMeta,
            List<String> imageUrlList,
            String username,
            List<String> tags) {
        return PostDetailResponse.from(post, postMeta, imageUrlList, username, tags);
    }

    public PostDetailResponse fromPostToPostDetailResponse(
            Post post, PostMetaResponse postMeta, List<String> imageUrlList, String username) {
        return PostDetailResponse.from(post, postMeta, imageUrlList, username, List.of());
    }

    public PostPersistResponse fromPostToPostPersistResponse(Post post) {
        return PostPersistResponse.from(post);
    }
}
