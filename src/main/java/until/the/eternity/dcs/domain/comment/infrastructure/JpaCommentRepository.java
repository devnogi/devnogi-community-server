package until.the.eternity.dcs.domain.comment.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import until.the.eternity.dcs.domain.comment.entity.Comment;

public interface JpaCommentRepository extends JpaRepository<Comment, Long> {
}
