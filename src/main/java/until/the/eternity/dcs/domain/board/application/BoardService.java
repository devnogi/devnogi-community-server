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
import until.the.eternity.dcs.domain.board.exception.BoardModifyForbiddenException;
import until.the.eternity.dcs.domain.board.exception.BoardNotFoundException;
import until.the.eternity.dcs.domain.user.application.UserService;
import until.the.eternity.dcs.domain.user.entity.UserSummary;
import until.the.eternity.dcs.domain.user.enums.UserGrade;

import java.util.List;

import static until.the.eternity.dcs.domain.user.enums.UserGrade.ADMIN;

@Service
@RequiredArgsConstructor
public class BoardService {
	private final BoardRepository boardRepository;
	private final BoardConverter boardConverter;
	private final UserService fakeUserService;

	public BoardPersistResponse createBoard(BoardCreateRequest request) {
		UserSummary user = getCurrentUser();
		checkManagerAuthority(user.getGrade());
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
		UserSummary user = getCurrentUser();
		checkManagerAuthority(user.getGrade());
		Board board = findBoardById(id);
		board.update(request.name(), request.description(), request.topCategory(), request.subCategory(), user.getId());
		return boardConverter.fromBoardToPersistResponse(board);
	}

	@Transactional
	public void deleteBoard(Long id) {
		UserSummary user = getCurrentUser();
		checkManagerAuthority(user.getGrade());
		Board board = findBoardById(id);
		board.delete(user.getId());
	}

	private UserSummary getCurrentUser() {
		return fakeUserService.getCurrentUser();
	}

	private void checkManagerAuthority(UserGrade grade) {
		if (!grade.equals(ADMIN)) {
			throw new BoardModifyForbiddenException();
		}
	}

	private Board findBoardById(Long id) {
		return boardRepository.findById(id)
			.orElseThrow(() -> new BoardNotFoundException(id));
	}
}
