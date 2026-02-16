package until.the.eternity.dcs.domain.tag.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import until.the.eternity.dcs.domain.tag.entity.Tag;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByName(String name);

    @Query(
            "SELECT t FROM Tag t "
                    + "JOIN PostTag pt ON t.id = pt.tag.id "
                    + "WHERE pt.post.id = :postId")
    List<Tag> findAllByPostId(@Param("postId") Long postId);
}
