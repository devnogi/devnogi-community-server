package until.the.eternity.dcs.domain.board.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import until.the.eternity.dcs.domain.board.entity.Board;
import until.the.eternity.dcs.domain.board.entity.BoardRepository;

@Repository
@RequiredArgsConstructor
public class BoardRepositoryImpl implements BoardRepository {
	private final JpaBoardRepository jpaRepository;

	@Override
	public Board save(Board board) {
		return jpaRepository.save(board);
	}
}
