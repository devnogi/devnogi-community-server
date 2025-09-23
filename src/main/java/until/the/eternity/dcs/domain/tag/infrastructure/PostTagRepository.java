package until.the.eternity.dcs.domain.tag.infrastructure;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import until.the.eternity.dcs.domain.tag.entity.PostTag;

public interface PostTagRepository extends JpaRepository<PostTag, Long> {

    @Modifying
    @Query("DELETE FROM PostTag pt WHERE pt.post.id IN :postIdList")
    void deleteAllByPostIdIn(@Param("postIdList") List<Long> postIdList);
}
