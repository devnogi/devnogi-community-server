package until.the.eternity.dcs.domain.post.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import until.the.eternity.dcs.domain.tag.entity.PostTag;

import java.util.List;

@Builder
@Schema(description = "게시글 수정 요청 DTO")
public class PostUpdateRequest {

    @Schema(description = "게시글 제목", example = "수정된 게시글 제목", maxLength = 200)
    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 200, message = "제목은 200자 이하로 입력해주세요.")
    private String title;

    @Schema(description = "게시글 내용", example = "수정된 게시글 내용입니다.", maxLength = 10000)
    @NotBlank(message = "내용은 필수입니다.")
    @Size(max = 10000, message = "내용은 10000자 이하로 입력해주세요.")
    private String content;

    @Schema(description = "임시저장 여부", example = "false")
    private Boolean isDraft;

    @Schema(description = "태그 목록", example = "[\"개발\", \"Spring\", \"JPA\"]")
    private List<PostTag> tags;
}
