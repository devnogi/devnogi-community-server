package until.the.eternity.dcs.domain.comment.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import until.the.eternity.dcs.domain.comment.entity.CommentLike;
import until.the.eternity.dcs.domain.comment.entity.CommentLikeRepository;

import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class CommentLikeRepositoryImpl implements CommentLikeRepository {
	private final JpaCommentLikeRepository jpaCommentLikeRepository;

	@Override
	public CommentLike findByCommentIdAndUserId(Long commentId, Long userId) {
		return jpaCommentLikeRepository.findByCommentIdAndUserId(commentId, userId)
			.orElse(null);
	}

	@Override
	public CommentLike save(CommentLike commentLike) {
		return jpaCommentLikeRepository.save(commentLike);
	}

	@Override
	public void deleteByCommentId(Long commentId) {
		jpaCommentLikeRepository.deleteByCommentId(commentId);
	}

	@Override
	public Set<Long> findIdsByUserIdAndCommentIdIn(Long userId, List<Long> commentIds) {
		return jpaCommentLikeRepository.findIdsByUserIdAndCommentIdIn(userId, commentIds);
	}
}
