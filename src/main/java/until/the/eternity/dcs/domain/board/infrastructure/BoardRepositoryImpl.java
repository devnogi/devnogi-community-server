package until.the.eternity.dcs.domain.board.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import until.the.eternity.dcs.domain.board.entity.Board;
import until.the.eternity.dcs.domain.board.entity.BoardRepository;
import until.the.eternity.dcs.domain.board.exception.BoardNotFoundException;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class BoardRepositoryImpl implements BoardRepository {
	private final JpaBoardRepository jpaRepository;

	@Override
	public Board save(Board board) {
		return jpaRepository.save(board);
	}

	@Override
	public List<Board> findAll() {
		return jpaRepository.findAllByOrderByTopCategoryAscSubCategoryAsc();
	}

	@Override
	public Board findById(Long id) {
		return jpaRepository.findById(id)
			.orElseThrow(() -> new BoardNotFoundException(id));
	}

	@Override
	public void deleteById(Long id) {
		jpaRepository.deleteById(id);
	}
}
