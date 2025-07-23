package until.the.eternity.dcs.domain.comment.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import until.the.eternity.dcs.domain.comment.entity.CommentMeta;

public interface JpaCommentMetaRepository extends JpaRepository<CommentMeta, Long> {}
