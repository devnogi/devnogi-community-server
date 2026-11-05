package until.the.eternity.dcs.domain.post.infrastructure;

import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import until.the.eternity.dcs.domain.post.entity.PostBookmark;

public interface PostBookmarkRepository extends JpaRepository<PostBookmark, Long> {
    boolean existsByUserIdAndPostId(Long userId, Long postId);

    @Modifying
    @Query(
            value =
                    "insert ignore into post_bookmark(user_id, post_id, created_at) values (:userId, :postId, current_timestamp(6))",
            nativeQuery = true)
    int insertIgnore(@Param("userId") Long userId, @Param("postId") Long postId);

    void deleteByUserIdAndPostId(Long userId, Long postId);

    @Query(
            "select b from PostBookmark b join fetch b.post p where b.userId = :userId and p.isDeleted = false and p.isBlocked = false and p.isDraft = false")
    Page<PostBookmark> findVisibleByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query(
            "select b.post.id from PostBookmark b where b.userId = :userId and b.post.id in :postIds")
    List<Long> findPostIdsByUserIdAndPostIdIn(
            @Param("userId") Long userId, @Param("postIds") Collection<Long> postIds);

    @Modifying
    @Query("delete from PostBookmark b where b.post.id in :postIds")
    void deleteAllByPostIdIn(@Param("postIds") Collection<Long> postIds);
}
