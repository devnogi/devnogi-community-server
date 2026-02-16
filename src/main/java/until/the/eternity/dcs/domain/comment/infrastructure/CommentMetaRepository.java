package until.the.eternity.dcs.domain.comment.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import until.the.eternity.dcs.domain.comment.entity.CommentMeta;

import java.util.List;

@Repository
public interface CommentMetaRepository extends JpaRepository<CommentMeta, Long> {

    List<CommentMeta> findByCommentIdIn(List<Long> commentIds);

    @Modifying
    @Query("DELETE FROM CommentMeta cm WHERE cm.commentId IN :commentIdList")
    void deleteAllByCommentIdIn(@Param("commentIdList") List<Long> commentIdList);
}
