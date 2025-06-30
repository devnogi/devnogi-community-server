package until.the.eternity.dcs.domain.post.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PostUpdateRequest(
        @Schema(description = "게시글 제목", example = "수정된 게시글 제목입니다.")
        @NotBlank(message = "제목은 필수입니다.")
        @Size(max = 200, message = "제목은 200자를 초과할 수 없습니다.")
        String title,

        @Schema(description = "게시글 내용", example = "수정된 게시글 내용입니다.")
        @NotBlank(message = "내용은 필수입니다.")
        @Size(max = 10000, message = "내용은 10000자를 초과할 수 없습니다.")
        String content,

        @Schema(description = "임시저장 여부", example = "false")
        Boolean isDraft,

        @Schema(description = "태그 목록")
        List<String> tags
) {
}
