package until.the.eternity.dcs.domain.board.entity;

import java.util.List;

public interface BoardRepository {
	Board save(Board board);

	List<Board> findAll();

	Board findById(Long id);

	void deleteById(Long id);
}
