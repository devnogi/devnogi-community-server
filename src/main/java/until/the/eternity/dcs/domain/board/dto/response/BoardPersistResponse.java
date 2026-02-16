package until.the.eternity.dcs.domain.board.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Builder
public record BoardPersistResponse(
        @Schema(description = "게시판 아이디", example = "1", requiredMode = REQUIRED) Long id) {
    public static BoardPersistResponse of(Long id) {
        return BoardPersistResponse.builder().id(id).build();
    }
}
