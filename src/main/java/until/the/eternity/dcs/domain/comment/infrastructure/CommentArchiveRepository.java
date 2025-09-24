package until.the.eternity.dcs.domain.comment.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import until.the.eternity.dcs.domain.comment.entity.CommentArchive;

public interface CommentArchiveRepository extends JpaRepository<CommentArchive, Long> {}
