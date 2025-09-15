package until.the.eternity.dcs.domain.post.presentation;

import static org.springframework.http.HttpStatus.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import until.the.eternity.dcs.common.request.CustomPageRequest;
import until.the.eternity.dcs.common.response.CustomPageResponse;
import until.the.eternity.dcs.domain.post.application.PostService;
import until.the.eternity.dcs.domain.post.dto.request.PostCreateRequest;
import until.the.eternity.dcs.domain.post.dto.request.PostLikeCreateRequest;
import until.the.eternity.dcs.domain.post.dto.request.PostUpdateRequest;
import until.the.eternity.dcs.domain.post.dto.response.PostDetailResponse;
import until.the.eternity.dcs.domain.post.dto.response.PostPersistResponse;
import until.the.eternity.dcs.domain.post.dto.response.PostSummaryResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostController {
    private final PostService postService;

    @PostMapping
    @Operation(
            summary = "게시글 생성 API",
            description = """
			- Description : 이 API는 게시글을 생성합니다.
			- Assignee : 고범수
		""")
    @ApiResponse(
            responseCode = "201",
            content = @Content(schema = @Schema(implementation = PostSummaryResponse.class)))
    public ResponseEntity<PostPersistResponse> createPost(
            @Valid @RequestBody PostCreateRequest request) {
        return ResponseEntity.status(CREATED).body(postService.createPost(request));
    }

    @GetMapping("/details/{id}")
    @Operation(
            summary = "게시글 단건 조회 API",
            description = """
			- Description : 이 API는 게시글을 단건 조회합니다.
			- Assignee : 고범수
		""")
    @ApiResponse(
            responseCode = "200",
            content = @Content(schema = @Schema(implementation = PostDetailResponse.class)))
    public PostDetailResponse getPost(@PathVariable Long id) {
        return postService.findPost(id);
    }

    @GetMapping
    @Operation(
            summary = "게시글 리스트 조회 API",
            description = """
			- Description : 이 API는 게시글 리스트를 조회합니다.
			- Assignee : 고범수
		""")
    @ApiResponse(
            responseCode = "200",
            content = @Content(schema = @Schema(implementation = PostSummaryResponse.class)))
    public CustomPageResponse<PostSummaryResponse> getPosts(
            @ModelAttribute CustomPageRequest request) {
        return CustomPageResponse.from(postService.findPosts(request));
    }

    @PatchMapping("/{id}")
    @Operation(
            summary = "게시글 수정 API",
            description = """
			- Description : 이 API는 게시글을 수정합니다.
			- Assignee : 고범수
		""")
    @ApiResponse(
            responseCode = "200",
            content = @Content(schema = @Schema(implementation = PostSummaryResponse.class)))
    public ResponseEntity<PostPersistResponse> updatePost(
            @PathVariable Long id, @Valid @RequestBody PostUpdateRequest postUpdateRequest) {
        return ResponseEntity.status(OK).body(postService.updatePost(id, postUpdateRequest));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "게시글 삭제 API",
            description = """
			- Description : 이 API는 게시글을 삭제합니다.
			- Assignee : 고범수
		""")
    @ApiResponse(responseCode = "204")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        postService.deletePost(id);
        return ResponseEntity.status(NO_CONTENT).build();
    }

    @PostMapping("like")
    @Operation(
            summary = "게시글 좋아요 토글 API",
            description = """
			- Description : 이 API는 게시글 토글 API 입니다.
			- Assignee : 고범수
		""")
    @ApiResponse(responseCode = "201")
    public ResponseEntity<Void> like(@RequestBody PostLikeCreateRequest postLikeCreateRequest) {
        postService.togglePostLike(postLikeCreateRequest);
        return ResponseEntity.status(NO_CONTENT).build();
    }

    @GetMapping("/{boardId}")
    @Operation(
            summary = "게시글 리스트 조회 API",
            description =
                    """
			- Description : 이 API는 게시판 별 게시글 리스트를 조회합니다.
			- Assignee : 고범수
		""")
    @ApiResponse(
            responseCode = "200",
            content = @Content(schema = @Schema(implementation = PostSummaryResponse.class)))
    public CustomPageResponse<PostSummaryResponse> getPostsByBoardId(
            @ModelAttribute CustomPageRequest request, @PathVariable Long boardId) {
        return CustomPageResponse.from(postService.findPostsByBoardId(request, boardId));
    }
}
