package until.the.eternity.dcs.domain.post.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record PostCreateRequest(
        @Schema(description = "레거시 게시판 ID") Long boardId,
        @Schema(description = "레거시 게시글 제목") @Size(max = 200) String title,
        @Schema(description = "게시글 내용", example = "게시글 내용입니다.") @NotBlank(message = "내용은 필수입니다.")
                String content,
        @Schema(description = "본문 형식", example = "PLAIN_TEXT_V1") String contentFormat,
        @Schema(description = "임시저장 여부", example = "false") Boolean isDraft,
        @Schema(description = "태그 목록") List<String> tags) {
    public PostCreateRequest(
            Long boardId, String title, String content, Boolean isDraft, List<String> tags) {
        this(boardId, title, content, null, isDraft, tags);
    }
}
