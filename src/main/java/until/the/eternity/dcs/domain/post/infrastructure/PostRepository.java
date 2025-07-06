package until.the.eternity.dcs.domain.post.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import until.the.eternity.dcs.domain.post.entity.Post;

import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    Optional<Post> findByIdAndIsDeletedFalseAndIsBlockedFalse(Long id);


}
