package until.the.eternity.dcs.domain.board.entity;

import java.util.List;
import java.util.Optional;

public interface BoardRepository {
	Board save(Board board);

	List<Board> findAll();

	Optional<Board> findById(Long id);

	void deleteById(Long id);
}
