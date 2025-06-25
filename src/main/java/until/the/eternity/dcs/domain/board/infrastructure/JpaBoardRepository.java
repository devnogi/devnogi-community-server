package until.the.eternity.dcs.domain.board.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import until.the.eternity.dcs.domain.board.entity.Board;

import java.util.List;

public interface JpaBoardRepository extends JpaRepository<Board, Long> {

	List<Board> findAllByOrderByTopCategoryAscSubCategoryAsc();

}
