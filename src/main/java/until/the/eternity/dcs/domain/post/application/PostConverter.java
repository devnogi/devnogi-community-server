package until.the.eternity.dcs.domain.post.application;

import org.springframework.stereotype.Component;
import until.the.eternity.dcs.domain.board.entity.Board;
import until.the.eternity.dcs.domain.post.dto.request.PostCreateRequest;
import until.the.eternity.dcs.domain.post.dto.response.PostDetailResponse;
import until.the.eternity.dcs.domain.post.dto.response.PostSummaryResponse;
import until.the.eternity.dcs.domain.post.entity.Post;
import until.the.eternity.dcs.domain.tag.entity.PostTag;


import java.util.List;

@Component
public class PostConverter {

    public Post fromCreateRequestToPost(PostCreateRequest request, Long userId, List<PostTag>postTagList) {
        Board board = Board.builder().id(request.boardId()).build();
        return Post.builder()
                .board(board)
                .userId(userId)
                .title(request.title())
                .content(request.content())
                .isDraft(request.isDraft())
                .postTags(postTagList)
                .build();
    }


    public PostSummaryResponse fromPostToPostSummaryResponse(Post post) {
        return PostSummaryResponse.from(post);
    }

    public PostDetailResponse fromPostToPostDetailResponse(Post post){
        return PostDetailResponse.from(post);
    }
}
