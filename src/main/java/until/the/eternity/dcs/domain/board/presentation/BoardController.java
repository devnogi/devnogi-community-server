package until.the.eternity.dcs.domain.board.presentation;

import lombok.RequiredArgsConstructor;
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
@RequestMapping("/api/board")
public class BoardController {
	private final BoardService boardService;

	@PostMapping
	public BoardPersistResponse createBoard(@RequestBody BoardCreateRequest request) {
		return boardService.createBoard(request);
	}

	@GetMapping
	public BoardListResponse getBoards() {
		return boardService.getAllBoards();
	}

	@PatchMapping("/{id}")
	public BoardPersistResponse updateBoard(@PathVariable("id") Long id, @RequestBody BoardUpdateRequest request) {
		return boardService.updateBoard(id, request);
	}

	@DeleteMapping("/{id}")
	public void deleteBoard(@PathVariable("id") Long id) {
		boardService.deleteBoard(id);
	}
}
