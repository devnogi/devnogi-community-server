package until.the.eternity.dcs.domain.comment.infrastructure;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import until.the.eternity.dcs.domain.comment.entity.Comment;

import java.util.Optional;

public interface JpaCommentRepository extends JpaRepository<Comment, Long> {

	Optional<Comment> findByIdAndIsDeletedIsFalse(Long id);

	Page<Comment> findAllByPostIdAndIsDeletedIsFalse(Long postId, Pageable pageable);
}
