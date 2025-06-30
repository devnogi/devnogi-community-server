package until.the.eternity.dcs.domain.comment.entity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface CommentRepository {
	Comment save(Comment comment);

	Optional<Comment> findById(Long id);

	Page<Comment> findByPost(Long postId, Pageable pageable);
}
