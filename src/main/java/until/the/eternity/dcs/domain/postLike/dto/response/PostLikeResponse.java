package until.the.eternity.dcs.domain.postLike.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record PostLikeResponse(

    @Schema(description = "게시글 좋아요 ID", example = "1")
    Long id

) {
}
