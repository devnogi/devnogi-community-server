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

    public PostSummaryResponse fromPostToPostSummaryResponse(Post post, PostMetaResponse postMeta) {
        return PostSummaryResponse.of(post, postMeta);
    }

    public PostDetailResponse fromPostToPostDetailResponse(
            Post post, PostMetaResponse postMeta, List<String> imageUrlList) {
        return PostDetailResponse.from(post, postMeta, imageUrlList);
    }

    public PostPersistResponse fromPostToPostPersistResponse(Post post) {
        return PostPersistResponse.from(post);
    }
}
