package until.the.eternity.dcs.domain.board.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import until.the.eternity.dcs.domain.board.entity.Board;

public interface JpaBoardRepository extends JpaRepository<Board, Long> {


}
