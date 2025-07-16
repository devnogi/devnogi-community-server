package until.the.eternity.dcs.domain.post.infrastructure;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import until.the.eternity.dcs.domain.post.entity.PostLike;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    Optional<PostLike> findByUserIdAndPostId(Long userId, Long postId);

    void deleteByUserIdAndPostId(Long userId, Long postId);

    boolean existsByUserIdAndPostId(Long userId, Long postId);
}
