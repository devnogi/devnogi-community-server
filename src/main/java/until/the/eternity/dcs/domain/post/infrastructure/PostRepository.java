package until.the.eternity.dcs.domain.post.infrastructure;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import until.the.eternity.dcs.domain.post.entity.Post;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    Optional<Post> findByIdAndIsDeletedFalseAndIsBlockedFalse(Long id);

    Page<Post> findAllByIsDeletedFalseAndIsBlockedFalse(Pageable pageable);
}
