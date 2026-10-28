package until.the.eternity.dcs.domain.board.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import until.the.eternity.dcs.domain.board.entity.BoardArchive;

public interface BoardArchiveRepository extends JpaRepository<BoardArchive, Long> {}
