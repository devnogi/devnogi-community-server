package until.the.eternity.dcs.domain.post.infrastructure;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import until.the.eternity.dcs.domain.post.entity.PostMeta;

public interface PostMetaRepository extends JpaRepository<PostMeta, Long> {

    Optional<PostMeta> findByPostId(Long postId);
}
