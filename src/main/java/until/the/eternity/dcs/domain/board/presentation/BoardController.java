package until.the.eternity.dcs.domain.board.presentation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import until.the.eternity.dcs.domain.board.application.BoardService;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/board")
public class BoardController {
	private final BoardService boardService;


}
