package until.the.eternity.dcs.domain.tag.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import until.the.eternity.dcs.domain.tag.application.TagService;
import until.the.eternity.dcs.domain.tag.dto.response.TagResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tags")
public class TagController {
    private final TagService tagService;

    @GetMapping("/{postId}")
    @Operation(
            summary = "태그 리스트 조회 API",
            description = """
			- Description : 이 API는 게시글 별 태그들을 조회합니다.
			- Assignee : 고범수
		""")
    @ApiResponse(
            responseCode = "200",
            content = @Content(schema = @Schema(implementation = TagResponse.class)))
    public List<TagResponse> findByPostId(@PathVariable("postId") Long postId) {
        return tagService.findByPostId(postId);
    }
}
