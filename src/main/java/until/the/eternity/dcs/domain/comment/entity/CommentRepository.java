package until.the.eternity.dcs.domain.comment.entity;

import java.util.Optional;

public interface CommentRepository {
	Comment save(Comment comment);

	Optional<Comment> findById(Long id);
}
