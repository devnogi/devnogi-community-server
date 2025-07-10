package until.the.eternity.dcs.domain.postLike.application;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import until.the.eternity.dcs.domain.post.entity.Post;
import until.the.eternity.dcs.domain.post.exception.PostNotFoundException;
import until.the.eternity.dcs.domain.post.infrastructure.PostRepository;
import until.the.eternity.dcs.domain.postLike.dto.request.PostLikeCreateRequest;
import until.the.eternity.dcs.domain.postLike.dto.response.PostLikeResponse;
import until.the.eternity.dcs.domain.postLike.entity.PostLike;

@Component
@RequiredArgsConstructor
public class PostLikeConverter {

    private PostRepository postRepository;

    public PostLike fromPostLikeCreateRequestToPostLike(PostLikeCreateRequest postLikeCreateRequest){

        Post post = postRepository.findByIdAndIsDeletedFalseAndIsBlockedFalse(postLikeCreateRequest.postId())
                .orElseThrow(()-> new PostNotFoundException(postLikeCreateRequest.postId()));

        return PostLike.builder()
                .post(post)
                .build();
    }

    public PostLikeResponse fromPostLikeToPostLikeResponse(PostLike postLike){
        return PostLikeResponse.builder()
                .id(postLike.getId())
                .build();
    }

}
