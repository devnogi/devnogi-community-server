package until.the.eternity.dcs.domain.comment.entity;

public interface CommentLikeRepository {

	CommentLike save(CommentLike commentLike);

	CommentLike findByCommentIdAndUserId(Long commentId, Long userId);

	void deleteByCommentId(Long commentId);
}
