package until.the.eternity.dcs.domain.comment.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import until.the.eternity.dcs.domain.comment.entity.CommentLike;
import until.the.eternity.dcs.domain.comment.entity.CommentLikeRepository;

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
}
