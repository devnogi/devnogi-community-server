package until.the.eternity.dcs.domain.post.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import until.the.eternity.dcs.common.config.QueryDslConfig;
import until.the.eternity.dcs.domain.post.entity.Post;
import until.the.eternity.dcs.domain.post.entity.PostMeta;

@DataJpaTest
@Import(QueryDslConfig.class)
class PostMetaRepositoryTest {
    @Autowired PostMetaRepository postMetaRepository;
    @Autowired EntityManager entityManager;

    @Test
    void incrementsAndDecrementsCommentCountInDatabase() {
        Post post =
                Post.builder()
                        .userId(1L)
                        .title("post")
                        .content("content")
                        .comments(new ArrayList<>())
                        .postTags(new ArrayList<>())
                        .build();
        post.setCreatedAt(LocalDateTime.now());
        entityManager.persist(post);
        entityManager.persist(PostMeta.create(post.getId(), 1));
        entityManager.flush();
        entityManager.clear();

        assertThat(postMetaRepository.incrementCommentCount(post.getId(), 1)).isEqualTo(1);
        assertThat(postMetaRepository.incrementCommentCount(post.getId(), -1)).isEqualTo(1);
        entityManager.clear();

        assertThat(postMetaRepository.findById(post.getId()).orElseThrow().getCommentCount())
                .isEqualTo(1);
    }
}
