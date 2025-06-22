package until.the.eternity.dcs.domain.board.dto.response;

import lombok.Builder;
import until.the.eternity.dcs.domain.board.entity.Board;

@Builder
public record BoardDetailResponse(
	String name,
	String description,
	String topCategory,
	String subCategory
) {
	public static BoardDetailResponse from(Board board) {
		return BoardDetailResponse.builder()
			.name(board.getName())
			.description(board.getDescription())
			.topCategory(board.getTopCategory())
			.subCategory(board.getSubCategory())
			.build();
	}
}