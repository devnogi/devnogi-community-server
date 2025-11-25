package until.the.eternity.dcs.domain.post.infrastructure;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import until.the.eternity.dcs.domain.post.entity.PostMeta;

public interface PostMetaRepository extends JpaRepository<PostMeta, Long> {

    PostMeta findByPostId(Long postId);

    @Modifying
    @Query("DELETE FROM PostMeta pm WHERE pm.postId IN :postIdList")
    void deleteAllByPostIdIn(@Param("postIdList") List<Long> postIdList);

    List<PostMeta> findAllByPostIdIn(List<Long> postIdList);
}
