package until.the.eternity.dcs.domain.comment.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import until.the.eternity.dcs.domain.comment.entity.CommentLike;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface JpaCommentLikeRepository extends JpaRepository<CommentLike, Long> {

	Optional<CommentLike> findByCommentIdAndUserId(Long commentId, Long userId);

	void deleteByCommentIdAndUserId(Long commentId, Long userId);

	@Query("SELECT cl.commentId FROM CommentLike cl WHERE cl.userId = :userId AND cl.commentId IN :commentIds")
	Set<Long> findIdsByUserIdAndCommentIdIn(Long userId, List<Long> commentIds);
}
