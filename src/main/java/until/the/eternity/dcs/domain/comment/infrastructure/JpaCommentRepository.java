package until.the.eternity.dcs.domain.comment.infrastructure;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import until.the.eternity.dcs.domain.comment.entity.Comment;

public interface JpaCommentRepository extends JpaRepository<Comment, Long> {

    Page<Comment> findAllByPostId(Long postId, Pageable pageable);

    List<Comment> findAllByIsDeletedTrueAndDeletedAtLessThanEqual(LocalDateTime date);

    @Modifying
    @Query("DELETE FROM Comment c WHERE c.id IN :commentIdList")
    void deleteAllByCommentIdIn(@Param("commentIdList") List<Long> commentIdList);
}
