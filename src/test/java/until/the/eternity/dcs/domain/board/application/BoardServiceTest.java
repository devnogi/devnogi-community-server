package until.the.eternity.dcs.domain.board.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import until.the.eternity.dcs.domain.board.dto.request.BoardCreateRequest;
import until.the.eternity.dcs.domain.board.dto.request.BoardUpdateRequest;
import until.the.eternity.dcs.domain.board.dto.response.BoardListResponse;
import until.the.eternity.dcs.domain.board.dto.response.BoardPersistResponse;
import until.the.eternity.dcs.domain.board.entity.Board;
import until.the.eternity.dcs.domain.board.infrastructure.BoardRepository;
import until.the.eternity.dcs.domain.user.entity.UserSummary;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static until.the.eternity.dcs.domain.user.enums.UserGrade.ADMIN;

class BoardServiceTest {
    BoardRepository boardRepository = mock(BoardRepository.class);
    BoardConverter boardConverter = new BoardConverter();
    BoardPermissionEvaluator boardPermissionEvaluator = mock(BoardPermissionEvaluator.class);
    BoardService boardService =
            new BoardService(boardRepository, boardConverter, boardPermissionEvaluator);

    Board board1;
    Board board2;
    Long boardId = 1L;
    String name = "board name";
    String description = "board description";
    String topCategory = "top category";
    String subCategory = "sub category";
    UserSummary user;
    Long userId = 1L;

    @BeforeEach
    void init() {
        board1 =
                Board.builder()
                        .id(boardId)
                        .name(name)
                        .description(description)
                        .topCategory(topCategory)
                        .subCategory(subCategory)
                        .createdBy(1L)
                        .build();
        board1.setCreatedAt(LocalDateTime.now());
        board1.setUpdatedAt(LocalDateTime.now());

        board2 =
                Board.builder()
                        .id(boardId + 1)
                        .name(name + "2")
                        .description(description + "2")
                        .topCategory(topCategory + "2")
                        .subCategory(subCategory + "2")
                        .createdBy(1L)
                        .build();
        board2.setCreatedAt(LocalDateTime.now());
        board2.setUpdatedAt(LocalDateTime.now());

        user = UserSummary.builder().id(1L).grade(ADMIN).build();
    }

    @Test
    @DisplayName("createBoard 는 새로운 Board를 저장한다.")
    void createBoard_Success() {
        // given
        when(boardRepository.save(any(Board.class))).thenReturn(board1);
        when(boardRepository.findByIdAndIsDeletedIsFalse(anyLong()))
                .thenReturn(Optional.of(board1));
        BoardCreateRequest request =
                new BoardCreateRequest(name, description, topCategory, subCategory);

        // when
        BoardPersistResponse response = boardService.createBoard(request);

        // then
        assertNotNull(response);
        assertEquals(boardId, response.id());

        Board board = boardRepository.findByIdAndIsDeletedIsFalse(response.id()).get();
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
        when(boardRepository.findAllByIsDeletedIsFalseOrderByTopCategoryAscSubCategoryAsc())
                .thenReturn(List.of(board1, board2));

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
        when(boardRepository.findByIdAndIsDeletedIsFalse(anyLong()))
                .thenReturn(Optional.of(board1));
        String newName = "new name";
        String newDescription = "new description";
        String newTopCategory = "new top category";
        String newSubCategory = "new sub category";
        BoardUpdateRequest request =
                new BoardUpdateRequest(newName, newDescription, newTopCategory, newSubCategory);

        // when
        BoardPersistResponse response = boardService.updateBoard(boardId, request);

        // then
        assertNotNull(response);
        assertEquals(1L, response.id());

        Board board = boardRepository.findByIdAndIsDeletedIsFalse(boardId).get();
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
        when(boardRepository.findByIdAndIsDeletedIsFalse(anyLong()))
                .thenReturn(Optional.of(board1));

        // when
        boardService.deleteBoard(boardId);

        // then
        Board board = boardRepository.findByIdAndIsDeletedIsFalse(boardId).get();
        assertTrue(board.getIsDeleted());
    }
}
