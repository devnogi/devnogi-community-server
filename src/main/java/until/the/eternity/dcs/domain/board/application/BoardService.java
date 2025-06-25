package until.the.eternity.dcs.domain.board.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import until.the.eternity.dcs.domain.board.dto.request.BoardCreateRequest;
import until.the.eternity.dcs.domain.board.dto.request.BoardUpdateRequest;
import until.the.eternity.dcs.domain.board.dto.response.BoardListResponse;
import until.the.eternity.dcs.domain.board.dto.response.BoardPersistResponse;
import until.the.eternity.dcs.domain.board.entity.Board;
import until.the.eternity.dcs.domain.board.entity.BoardRepository;
import until.the.eternity.dcs.domain.user.entity.UserSummary;
import until.the.eternity.dcs.domain.user.application.UserService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardService {
	private final BoardRepository boardRepository;
	private final BoardConverter boardConverter;
	private final UserService fakeUserService;

	public BoardPersistResponse createBoard(BoardCreateRequest request) {
		UserSummary user = fakeUserService.getCurrentUser();
		Board board = boardConverter.fromCreateRequestToBoard(request, user.getId());
		Board saved = boardRepository.save(board);
		return boardConverter.fromBoardToPersistResponse(saved);
	}

	public BoardListResponse getAllBoards() {
		List<Board> boardList = boardRepository.findAll();
		return boardConverter.fromBoardToListResponse(boardList);
	}

	@Transactional
	public BoardPersistResponse updateBoard(Long id, BoardUpdateRequest request) {
		Board board = findBoardById(id);
		checkManagerAuthority();
		board.update(request.name(), request.description(), request.topCategory(), request.subCategory());
		return boardConverter.fromBoardToPersistResponse(board);
	}

	public void deleteBoard(Long id) {
		checkManagerAuthority();
		boardRepository.deleteById(id);
	}

	private void checkManagerAuthority() {
		UserSummary user = fakeUserService.getCurrentUser();
		if (!user.getGrade().equals("manager"))
			throw new RuntimeException("Only manager user can modify or delete board");
	}

	private Board findBoardById(Long id) {
		return boardRepository.findById(id);
	}
}
