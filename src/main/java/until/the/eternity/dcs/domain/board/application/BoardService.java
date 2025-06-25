package until.the.eternity.dcs.domain.board.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import until.the.eternity.dcs.domain.board.dto.request.BoardCreateRequest;
import until.the.eternity.dcs.domain.board.dto.response.BoardListResponse;
import until.the.eternity.dcs.domain.board.dto.response.BoardPersistResponse;
import until.the.eternity.dcs.domain.board.entity.Board;
import until.the.eternity.dcs.domain.board.entity.BoardRepository;
import until.the.eternity.dcs.domain.user.entity.UserSummary;
import until.the.eternity.dcs.domain.user.fake.FakeUserService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardService {
	private final BoardRepository boardRepository;
	private final BoardConverter boardConverter;
	// todo User 관련 기능 구현 후 Fake에서 진짜 서비스로 교체 필요
	private final FakeUserService fakeUserService;

	public BoardPersistResponse createBoard(BoardCreateRequest request) {
		UserSummary user = fakeUserService.getUser();
		Board board = boardConverter.fromCreateRequestToBoard(request, user.getId());
		Board saved = boardRepository.save(board);
		return boardConverter.fromBoardToPersistResponse(saved);
	}

	public BoardListResponse getAllBoards() {
		List<Board> boardList = boardRepository.findAll();
		return boardConverter.fromBoardToListResponse(boardList);
	}
}
