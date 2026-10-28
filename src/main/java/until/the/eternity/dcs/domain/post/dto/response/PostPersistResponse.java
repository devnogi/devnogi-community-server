package until.the.eternity.dcs.domain.post.dto.response;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import until.the.eternity.dcs.domain.post.entity.Post;

@Builder
public record PostPersistResponse(
        @Schema(description = "게시글 아이디", example = "1", requiredMode = REQUIRED) Long id) {

    public static PostPersistResponse from(Post Post) {
        return new PostPersistResponse(Post.getId());
    }
}
