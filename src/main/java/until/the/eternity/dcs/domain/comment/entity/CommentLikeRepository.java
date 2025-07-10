package until.the.eternity.dcs.domain.comment.entity;

public interface CommentLikeRepository {

	CommentLike findCommentLikeByCommentIdAndUserId(Long commentId, Long userId);

	CommentLike save(CommentLike commentLike);

	void deleteById(Long id);
}
