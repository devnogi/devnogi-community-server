package until.the.eternity.dcs.domain.post.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import until.the.eternity.dcs.domain.post.entity.PostArchive;

public interface PostArchiveRepository extends JpaRepository<PostArchive, Long> {}
