package until.the.eternity.dcs.domain.board.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import until.the.eternity.dcs.domain.board.entity.Board;

import java.util.List;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Builder
public record BoardListResponse(
	@Schema(description = "게시판의 개수", example = "1", requiredMode = REQUIRED)
	int count,

	@Schema(description = "게시판 리스트",
		example = """
				[{
					"id": 1,
					"name": "길드원 모집",
					"description": "길드나 길드원을 모집하는 게시판입니다.",
			        "topCategory": "자유 게시판",
			        "subCategory": "길드 게시판"
				}]
			""",
		requiredMode = REQUIRED)
	List<BoardDetailResponse> boards
) {
	public static BoardListResponse from(List<Board> boardList) {
		return BoardListResponse.builder()
			.count(boardList.size())
			.boards(
				boardList.stream()
					.map(BoardDetailResponse::from)
					.toList()
			).build();
	}
}
