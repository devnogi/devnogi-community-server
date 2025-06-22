package until.the.eternity.dcs.domain.board.dto.response;

import lombok.Builder;
import until.the.eternity.dcs.domain.board.entity.Board;

import java.util.List;

@Builder
public record BoardListResponse(
	int number,
	List<BoardDetailResponse> data
) {
	public static BoardListResponse from(List<Board> boardList) {
		return BoardListResponse.builder()
			.number(boardList.size())
			.data(
				boardList.stream()
					.map(BoardDetailResponse::from)
					.toList()
			).build();
	}
}
