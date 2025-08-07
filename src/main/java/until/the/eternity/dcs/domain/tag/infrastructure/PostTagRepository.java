package until.the.eternity.dcs.domain.tag.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import until.the.eternity.dcs.domain.tag.entity.PostTag;

public interface PostTagRepository extends JpaRepository<PostTag, Long> {}
