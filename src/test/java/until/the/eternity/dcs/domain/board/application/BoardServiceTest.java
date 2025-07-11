package until.the.eternity.dcs.domain.board.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import until.the.eternity.dcs.domain.board.dto.request.BoardCreateRequest;
import until.the.eternity.dcs.domain.board.dto.request.BoardUpdateRequest;
import until.the.eternity.dcs.domain.board.dto.response.BoardListResponse;
import until.the.eternity.dcs.domain.board.dto.response.BoardPersistResponse;
import until.the.eternity.dcs.domain.board.entity.Board;
import until.the.eternity.dcs.domain.board.entity.BoardRepository;
import until.the.eternity.dcs.domain.user.application.UserService;
import until.the.eternity.dcs.domain.user.fake.FakeUserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BoardServiceTest {
	@Mock
	static BoardRepository boardRepository = mock(BoardRepository.class);
	static BoardConverter boardConverter = new BoardConverter();
	static UserService userService = new FakeUserService();

	@InjectMocks
	BoardService boardService = new BoardService(boardRepository, boardConverter, userService);

	static Board board1;
	static Board board2;
	static Long boardId = 1L;
	static String name = "board name";
	static String description = "board description";
	static String topCategory = "top category";
	static String subCategory = "sub category";

	@BeforeEach
	void init() {
		board1 = Board.builder()
			.id(boardId)
			.name(name)
			.description(description)
			.topCategory(topCategory)
			.subCategory(subCategory)
			.createdBy(1L)
			.build();
		board1.setCreatedAt(LocalDateTime.now());
		board1.setUpdatedAt(LocalDateTime.now());

		board2 = Board.builder()
			.id(boardId + 1)
			.name(name + "2")
			.description(description + "2")
			.topCategory(topCategory + "2")
			.subCategory(subCategory + "2")
			.createdBy(1L)
			.build();
		board2.setCreatedAt(LocalDateTime.now());
		board2.setUpdatedAt(LocalDateTime.now());
	}

	@Test
	@DisplayName("createBoard 는 새로운 Board를 저장한다.")
	void createBoard_Success() {
		// given
		when(boardRepository.save(any(Board.class))).thenReturn(board1);
		when(boardRepository.findById(anyLong())).thenReturn(Optional.of(board1));
		BoardCreateRequest request = new BoardCreateRequest(name, description, topCategory, subCategory);

		// when
		BoardPersistResponse response = boardService.createBoard(request);

		// then
		assertNotNull(response);
		assertEquals(boardId, response.id());

		Board board = boardRepository.findById(response.id()).get();
		assertNotNull(board);
		assertEquals(name, board.getName());
		assertEquals(description, board.getDescription());
		assertEquals(topCategory, board.getTopCategory());
		assertEquals(subCategory, board.getSubCategory());
		assertNotNull(board.getCreatedAt());
		assertNotNull(board.getCreatedBy());
		assertNotNull(board.getUpdatedAt());
	}

	@Test
	@DisplayName("getAllBoards 는 DB 에 저장된 모든 데이터를 BoardListResponse 로 조회한다.")
	void getAllBoards_Success() {
		// given
		when(boardRepository.findAll()).thenReturn(List.of(board1, board2));

		// when
		BoardListResponse response = boardService.getAllBoards();

		// then
		assertNotNull(response);
		assertEquals(2, response.count());
		assertEquals(1L, response.boards().get(0).id());
		assertEquals(name, response.boards().get(0).name());
		assertEquals(description, response.boards().get(0).description());
		assertEquals(topCategory, response.boards().get(0).topCategory());
		assertEquals(subCategory, response.boards().get(0).subCategory());
	}

	@Test
	@DisplayName("updateBoard 는 Board 의 정보를 변경한다.")
	void updateBoard_Success() {
		// given
		when(boardRepository.findById(anyLong())).thenReturn(Optional.of(board1));
		String newName = "new name";
		String newDescription = "new description";
		String newTopCategory = "new top category";
		String newSubCategory = "new sub category";
		BoardUpdateRequest request = new BoardUpdateRequest(newName, newDescription, newTopCategory, newSubCategory);

		// when
		BoardPersistResponse response = boardService.updateBoard(boardId, request);

		// then
		assertNotNull(response);
		assertEquals(1L, response.id());

		Board board = boardRepository.findById(boardId).get();
		assertNotNull(board);
		assertEquals(newName, board.getName());
		assertEquals(newDescription, board.getDescription());
		assertEquals(newTopCategory, board.getTopCategory());
		assertEquals(newSubCategory, board.getSubCategory());
	}

	@Test
	@DisplayName("deleteBoard 는 게시판을 삭제한다.")
	void deleteBoard_throws_BoardNotFoundException() {
		// given
		when(boardRepository.findById(anyLong())).thenReturn(Optional.of(board1));

		// when
		boardService.deleteBoard(boardId);

		// then
		Board board = boardRepository.findById(boardId).get();
		assertTrue(board.getIsDeleted());
	}
}