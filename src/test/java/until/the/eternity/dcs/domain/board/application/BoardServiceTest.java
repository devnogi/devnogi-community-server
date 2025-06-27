package until.the.eternity.dcs.domain.board.application;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import until.the.eternity.dcs.domain.board.dto.request.BoardCreateRequest;
import until.the.eternity.dcs.domain.board.dto.request.BoardUpdateRequest;
import until.the.eternity.dcs.domain.board.dto.response.BoardListResponse;
import until.the.eternity.dcs.domain.board.dto.response.BoardPersistResponse;
import until.the.eternity.dcs.domain.board.entity.Board;
import until.the.eternity.dcs.domain.board.exception.BoardNotFoundException;
import until.the.eternity.dcs.domain.user.application.UserService;
import until.the.eternity.dcs.domain.user.fake.FakeUserService;
import until.the.eternity.dcs.infrastructure.FakeBoardRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class BoardServiceTest {
	static BoardService boardService;
	static FakeBoardRepository boardRepository;
	static String name = "board name";
	static String description = "board description";
	static String topCategory = "top category";
	static String subCategory = "sub category";

	@BeforeAll
	static void setUp() {
		boardRepository = new FakeBoardRepository();
		BoardConverter boardConverter = new BoardConverter();
		UserService userService = new FakeUserService();
		boardService = new BoardService(boardRepository, boardConverter, userService);
	}

	@BeforeEach
	void init() {
		boardRepository.clearDbForTest();

		Board board = Board.builder()
			.name(name)
			.description(description)
			.topCategory(topCategory)
			.subCategory(subCategory)
			.build();
		boardRepository.save(board);
	}

	@Test
	@DisplayName("createBoard 는 새로운 Board를 저장한다.")
	void createBoard_Success() {
		// given
		BoardCreateRequest request = new BoardCreateRequest(name, description, topCategory, subCategory);

		// when
		BoardPersistResponse response = boardService.createBoard(request);

		// then
		assertNotNull(response);
		assertEquals(2L, response.id());

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
		// when
		BoardListResponse response = boardService.getAllBoards();

		// then
		assertNotNull(response);
		assertEquals(1, response.count());
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
		String newName = "new name";
		String newDescription = "new description";
		String newTopCategory = "new top category";
		String newSubCategory = "new sub category";
		BoardUpdateRequest request = new BoardUpdateRequest(newName, newDescription, newTopCategory, newSubCategory);
		Long boardId = 1L;

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
	@DisplayName("deleteBoard 는 게시판을 삭제하고, 조회 시 BoardNotFoundException 을 발생시킵니다.")
	void deleteBoard_throws_BoardNotFoundException() {
		// given
		Long boardId = 1L;

		// when
		boardService.deleteBoard(boardId);

		// then
		Assertions.assertThatThrownBy(() -> boardService.deleteBoard(boardId))
			.isInstanceOf(BoardNotFoundException.class);
	}
}