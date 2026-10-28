package until.the.eternity.dcs.domain.post.application;

import org.springframework.stereotype.Component;
import until.the.eternity.dcs.domain.post.entity.Post;
import until.the.eternity.dcs.domain.post.entity.PostLike;

@Component
public class PostLikeConverter {

    public PostLike toEntity(Long userId, Post post) {
        return PostLike.builder().userId(userId).post(post).build();
    }
}
