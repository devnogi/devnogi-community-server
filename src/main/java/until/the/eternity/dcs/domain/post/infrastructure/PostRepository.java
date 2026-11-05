package until.the.eternity.dcs.domain.post.infrastructure;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import until.the.eternity.dcs.domain.post.entity.Post;

@Repository
public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {

    Optional<Post> findByIdAndIsDeletedFalseAndIsBlockedFalse(Long id);

    Page<Post> findAllByIsDeletedFalseAndIsBlockedFalse(Pageable pageable);

    Page<Post> findAllByIsDeletedFalseAndIsBlockedFalseAndIsDraftFalse(Pageable pageable);

    Page<Post> findAllByUserIdAndIsDeletedFalseAndIsBlockedFalseAndIsDraftFalse(
            Long userId, Pageable pageable);

    @Query(
            value =
                    "select p from Post p where p.isDeleted = false and p.isBlocked = false and p.isDraft = false and (:keyword is null or :keyword = '' or lower(concat(coalesce(p.title, ''), ' ', coalesce(p.plainContent, p.content, ''))) like lower(concat('%', :keyword, '%')))",
            countQuery =
                    "select count(p) from Post p where p.isDeleted = false and p.isBlocked = false and p.isDraft = false and (:keyword is null or :keyword = '' or lower(concat(coalesce(p.title, ''), ' ', coalesce(p.plainContent, p.content, ''))) like lower(concat('%', :keyword, '%')))")
    Page<Post> findFeed(@Param("keyword") String keyword, Pageable pageable);

    Page<Post> findAllByBoardIdAndIsDeletedFalseAndIsBlockedFalse(Pageable pageable, Long id);

    List<Post> findAllByIsDeletedTrueAndDeletedAtLessThanEqual(LocalDateTime date);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Post p WHERE p.id IN :ids")
    void deleteAllByIdIn(@Param("ids") List<Long> ids);
}
