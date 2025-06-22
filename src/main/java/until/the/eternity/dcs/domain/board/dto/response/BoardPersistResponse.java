package until.the.eternity.dcs.domain.board.dto.response;

import lombok.Builder;

@Builder
public record BoardPersistResponse(
	Long id
) {
	public static BoardPersistResponse of(Long id) {
		return BoardPersistResponse.builder()
			.id(id)
			.build();
	}
}
