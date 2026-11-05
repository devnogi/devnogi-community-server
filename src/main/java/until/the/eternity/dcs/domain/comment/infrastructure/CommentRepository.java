package until.the.eternity.dcs.domain.comment.infrastructure;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import until.the.eternity.dcs.domain.comment.entity.Comment;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    Page<Comment> findAllByPostId(Long postId, Pageable pageable);

    Page<Comment> findAllByPostIdAndParentCommentIsNull(Long postId, Pageable pageable);

    Page<Comment> findAllByPostIdAndParentCommentId(
            Long postId, Long parentCommentId, Pageable pageable);

    List<Comment> findTop3ByPostIdAndParentCommentIdOrderByCreatedAtAsc(
            Long postId, Long parentCommentId);

    @Query(
            value =
                    """
                    SELECT ranked.id
                    FROM (
                        SELECT c.id,
                               c.parent_comment_id,
                               ROW_NUMBER() OVER (
                                   PARTITION BY c.parent_comment_id
                                   ORDER BY c.created_at ASC, c.id ASC
                               ) AS reply_order
                        FROM comment c
                        WHERE c.post_id = :postId
                          AND c.parent_comment_id IN (:parentIds)
                    ) ranked
                    WHERE ranked.reply_order <= 3
                    ORDER BY ranked.parent_comment_id ASC, ranked.reply_order ASC
                    """,
            nativeQuery = true)
    List<Long> findFirst3ReplyIdsByPostIdAndParentIds(
            @Param("postId") Long postId, @Param("parentIds") List<Long> parentIds);

    @Query("select c from Comment c where c.id in :ids")
    List<Comment> findAllByIdIn(@Param("ids") List<Long> ids);

    @Query(
            """
            select c.parentComment.id as parentId, count(c.id) as replyCount
            from Comment c
            where c.post.id = :postId and c.parentComment.id in :parentIds
            group by c.parentComment.id
            """)
    List<CommentReplyCount> countRepliesByPostIdAndParentIds(
            @Param("postId") Long postId, @Param("parentIds") List<Long> parentIds);

    long countByPostIdAndParentCommentId(Long postId, Long parentCommentId);

    @EntityGraph(attributePaths = "post")
    @Query(
            value =
                    """
                    SELECT c
                    FROM Comment c
                    JOIN c.post p
                    WHERE c.userId = :userId
                      AND c.isDeleted = false
                      AND c.isBlocked = false
                      AND p.isDeleted = false
                      AND p.isBlocked = false
                      AND p.isDraft = false
                    """,
            countQuery =
                    """
                    SELECT COUNT(c)
                    FROM Comment c
                    JOIN c.post p
                    WHERE c.userId = :userId
                      AND c.isDeleted = false
                      AND c.isBlocked = false
                      AND p.isDeleted = false
                      AND p.isBlocked = false
                      AND p.isDraft = false
                    """)
    Page<Comment> findVisibleByUserId(@Param("userId") Long userId, Pageable pageable);

    List<Comment> findAllByIsDeletedTrueAndDeletedAtLessThanEqual(LocalDateTime date);

    @Modifying
    @Query("DELETE FROM Comment c WHERE c.id IN :commentIdList")
    void deleteAllByCommentIdIn(@Param("commentIdList") List<Long> commentIdList);

    Integer countByPostIdAndIsDeletedFalse(Long postId);
}
