package until.the.eternity.dcs.domain.comment.entity;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import until.the.eternity.dcs.domain.comment.infrastructure.JpaCommentLikeRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class CommentLikeRepository {
	private final JpaCommentLikeRepository jpaCommentLikeRepository;

	public Optional<CommentLike> findByCommentIdAndUserId(Long commentId, Long userId) {
		return jpaCommentLikeRepository.findByCommentIdAndUserId(commentId, userId);
	}

	public CommentLike save(CommentLike commentLike) {
		return jpaCommentLikeRepository.save(commentLike);
	}

	public void deleteByCommentIdAndUserId(Long commentId, Long userId) {
		jpaCommentLikeRepository.deleteByCommentIdAndUserId(commentId, userId);
	}

	public Set<Long> findIdsByUserIdAndCommentIdIn(Long userId, List<Long> commentIds) {
		return jpaCommentLikeRepository.findIdsByUserIdAndCommentIdIn(userId, commentIds);
	}
}
