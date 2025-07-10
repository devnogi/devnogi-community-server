package until.the.eternity.dcs.domain.comment.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import until.the.eternity.dcs.domain.comment.entity.CommentLike;

import java.util.Optional;

public interface JpaCommentLikeRepository extends JpaRepository<CommentLike, Long> {

	Optional<CommentLike> findByCommentIdAndUserId(Long commentId, Long userId);
}
