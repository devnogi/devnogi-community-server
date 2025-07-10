package until.the.eternity.dcs.infrastructure;

import until.the.eternity.dcs.domain.comment.entity.CommentLike;
import until.the.eternity.dcs.domain.comment.entity.CommentLikeRepository;

public class FakeCommentLikeRepository implements CommentLikeRepository {
	@Override
	public CommentLike findCommentLikeByCommentIdAndUserId(Long commentId, Long userId) {
		return null;
	}

	@Override
	public CommentLike save(CommentLike commentLike) {
		return null;
	}

	@Override
	public void deleteById(Long id) {

	}
}
