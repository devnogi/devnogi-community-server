package until.the.eternity.dcs.domain.comment.entity;

import java.util.List;
import java.util.Set;

public interface CommentLikeRepository {

	CommentLike save(CommentLike commentLike);

	CommentLike findByCommentIdAndUserId(Long commentId, Long userId);

	void deleteByCommentIdAndUserId(Long commentId, Long userId);

	Set<Long> findIdsByUserIdAndCommentIdIn(Long userId, List<Long> commentIds);
}
