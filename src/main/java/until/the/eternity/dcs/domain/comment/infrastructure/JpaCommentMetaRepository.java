package until.the.eternity.dcs.domain.comment.infrastructure;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import until.the.eternity.dcs.domain.comment.entity.CommentMeta;

public interface JpaCommentMetaRepository extends JpaRepository<CommentMeta, Long> {

    List<CommentMeta> findByCommentIdIn(List<Long> commentIds);
}
