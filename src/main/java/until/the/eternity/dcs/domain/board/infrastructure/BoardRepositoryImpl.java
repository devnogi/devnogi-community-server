package until.the.eternity.dcs.domain.board.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import until.the.eternity.dcs.domain.board.entity.Board;
import until.the.eternity.dcs.domain.board.entity.BoardRepository;
import until.the.eternity.dcs.domain.board.exception.BoardNotFoundException;

import java.util.List;
import java.util.Optional;

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
		return jpaRepository.findAllByIsDeletedIsFalseOrderByTopCategoryAscSubCategoryAsc();
	}

	@Override
	public Optional<Board> findById(Long id) {
		return jpaRepository.findByIdAndIsDeletedIsFalse(id);
	}

	@Override
	public void deleteById(Long id) {
		jpaRepository.deleteById(id);
	}
}
