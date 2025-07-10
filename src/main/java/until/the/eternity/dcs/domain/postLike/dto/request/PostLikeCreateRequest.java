package until.the.eternity.dcs.domain.postLike.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record PostLikeCreateRequest(

    @Schema(description = "게시글 ID", example = "1")
    @NotNull(message = "게시글 ID는 필수입니다.")
    Long postId

) {

}