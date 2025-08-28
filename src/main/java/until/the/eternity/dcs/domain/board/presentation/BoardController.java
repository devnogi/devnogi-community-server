package until.the.eternity.dcs.domain.board.presentation;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import until.the.eternity.dcs.domain.board.application.BoardService;
import until.the.eternity.dcs.domain.board.dto.request.BoardCreateRequest;
import until.the.eternity.dcs.domain.board.dto.request.BoardUpdateRequest;
import until.the.eternity.dcs.domain.board.dto.response.BoardListResponse;
import until.the.eternity.dcs.domain.board.dto.response.BoardPersistResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/boards")
public class BoardController {
    private final BoardService boardService;

    @PostMapping
    @Operation(
            summary = "게시판 생성 API",
            description = """
			- Description : 이 API는 게시판을 생성합니다.
			- Assignee : 이신행
		""")
    @ApiResponse(
            responseCode = "201",
            content = @Content(schema = @Schema(implementation = BoardPersistResponse.class)))
    public ResponseEntity<BoardPersistResponse> createBoard(
            @RequestBody BoardCreateRequest request) {
        return ResponseEntity.status(CREATED).body(boardService.createBoard(request));
    }

    @GetMapping
    @Operation(
            summary = "게시판 조회 API",
            description =
                    """
			- Description : 이 API는 게시판을 topCategory-subCategory 순으로 정렬, 조회합니다.
			- Assignee : 이신행
		""")
    @ApiResponse(
            responseCode = "200",
            content = @Content(schema = @Schema(implementation = BoardListResponse.class)))
    public BoardListResponse getBoards() {
        return boardService.getAllBoards();
    }

    @PatchMapping("/{id}")
    @Operation(
            summary = "게시판 수정 API",
            description = """
			- Description : 이 API는 게시판을 수정합니다.
			- Assignee : 이신행
		""")
    @ApiResponse(
            responseCode = "200",
            content = @Content(schema = @Schema(implementation = BoardPersistResponse.class)))
    public BoardPersistResponse updateBoard(
            @PathVariable("id") Long id, @RequestBody BoardUpdateRequest request) {
        return boardService.updateBoard(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "게시판 삭제 API",
            description = """
			- Description : 이 API는 게시판을 삭제합니다.
			- Assignee : 이신행
		""")
    @ApiResponse(responseCode = "204")
    public ResponseEntity<Void> deleteBoard(@PathVariable("id") Long id) {
        boardService.deleteBoard(id);
        return ResponseEntity.status(NO_CONTENT).build();
    }
}
