package until.the.eternity.dcs.domain.board.application;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import until.the.eternity.dcs.domain.board.dto.request.BoardCreateRequest;
import until.the.eternity.dcs.domain.board.dto.response.BoardListResponse;
import until.the.eternity.dcs.domain.board.dto.response.BoardPersistResponse;
import until.the.eternity.dcs.domain.board.entity.Board;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class BoardConverterTest {
	static BoardConverter boardConverter;
	static String name = "board name";
	static String description = "board description";
	static String topCategory = "top category";
	static String subCategory = "sub category";

	@BeforeAll
	static void setUp() {
		boardConverter = new BoardConverter();
	}

	@Test
	@DisplayName("BoardCreateRequest 에서 Board 로 변경할 수 있다.")
	void fromCreateRequestToBoard_Success() {
		// given
		BoardCreateRequest request = new BoardCreateRequest(name, description, topCategory, subCategory);
		Long userId = 1L;

		// when
		Board board = boardConverter.fromCreateRequestToBoard(request, userId);

		// then
		assertNotNull(board);
		assertEquals(board.getName(), name);
		assertEquals(board.getDescription(), description);
		assertEquals(board.getTopCategory(), topCategory);
		assertEquals(board.getSubCategory(), subCategory);
	}

	@Test
	@DisplayName("List<Board> 에서 BoardListResponse 로 변경할 수 있다.")
	void fromBoardToListResponse_Success() {
		// given
		List<Board> boardList = new ArrayList<>();
		Board board1 = Board.builder().name(name).build();
		Board board2 = Board.builder().name(name + "2").build();
		boardList.add(board1);
		boardList.add(board2);

		// when
		BoardListResponse response = boardConverter.fromBoardToListResponse(boardList);

		// then
		assertNotNull(response);
		assertEquals(response.count(), 2);
		assertEquals(response.boards().get(0).name(), name);
		assertEquals(response.boards().get(1).name(), name + "2");
	}


	@Test
	@DisplayName("Board 에서 BoardPersistResponse 로 변경할 수 있다.")
	void fromBoardToPersistResponse_Success() {
		// given
		Board board = Board.builder().id(1L).build();

		// when
		BoardPersistResponse response = boardConverter.fromBoardToPersistResponse(board);

		// then
		assertNotNull(response);
		assertEquals(response.id(), 1L);
	}
}