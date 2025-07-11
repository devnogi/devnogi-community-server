package until.the.eternity.dcs.domain.post.application;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import until.the.eternity.dcs.domain.post.entity.Post;
import until.the.eternity.dcs.domain.post.exception.PostNotFoundException;
import until.the.eternity.dcs.domain.post.infrastructure.PostRepository;
import until.the.eternity.dcs.domain.post.dto.request.PostLikeCreateRequest;
import until.the.eternity.dcs.domain.postLike.entity.PostLike;

@Component
@RequiredArgsConstructor
public class PostLikeConverter {

    private final PostRepository postRepository;

    public PostLike fromPostLikeCreateRequestToPostLike(PostLikeCreateRequest postLikeCreateRequest){

        Post post = postRepository.findByIdAndIsDeletedFalseAndIsBlockedFalse(postLikeCreateRequest.postId())
                .orElseThrow(()-> new PostNotFoundException(postLikeCreateRequest.postId()));

        return PostLike.builder()
                .post(post)
                .build();
    }

}
