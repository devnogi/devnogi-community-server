package until.the.eternity.dcs.domain.comment.infrastructure;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import until.the.eternity.dcs.domain.comment.entity.CommentMeta;

public interface JpaCommentMetaRepository extends JpaRepository<CommentMeta, Long> {

    @Query("SELECT cm FROM CommentMeta cm WHERE cm.commentId IN :commentIds")
    List<CommentMeta> findByCommentIdIn(List<Long> commentIds);
}
