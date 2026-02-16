package until.the.eternity.dcs.domain.board.application;

import org.springframework.stereotype.Component;
import until.the.eternity.dcs.domain.board.dto.request.BoardCreateRequest;
import until.the.eternity.dcs.domain.board.dto.response.BoardListResponse;
import until.the.eternity.dcs.domain.board.dto.response.BoardPersistResponse;
import until.the.eternity.dcs.domain.board.entity.Board;

import java.util.List;

@Component
public class BoardConverter {
    public Board fromCreateRequestToBoard(BoardCreateRequest request, Long userId) {
        return Board.builder()
                .name(request.name())
                .description(request.description())
                .topCategory(request.topCategory())
                .subCategory(request.subCategory())
                .createdBy(userId)
                .build();
    }

    public BoardListResponse fromBoardToListResponse(List<Board> boardList) {
        return BoardListResponse.from(boardList);
    }

    public BoardPersistResponse fromBoardToPersistResponse(Board board) {
        return BoardPersistResponse.of(board.getId());
    }
}
