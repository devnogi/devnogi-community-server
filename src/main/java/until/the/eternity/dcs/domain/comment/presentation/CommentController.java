package until.the.eternity.dcs.domain.comment.presentation;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import until.the.eternity.dcs.common.request.CustomPageRequest;
import until.the.eternity.dcs.common.response.CustomPageResponse;
import until.the.eternity.dcs.domain.comment.application.CommentService;
import until.the.eternity.dcs.domain.comment.dto.request.CommentCreateRequest;
import until.the.eternity.dcs.domain.comment.dto.request.CommentLikeToggleRequest;
import until.the.eternity.dcs.domain.comment.dto.request.CommentUpdateRequest;
import until.the.eternity.dcs.domain.comment.dto.response.CommentPageResponseItem;
import until.the.eternity.dcs.domain.comment.dto.response.CommentPersistResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/comment")
public class CommentController {
    private final CommentService commentService;

    @PostMapping("/{postId}")
    @Operation(
            summary = "댓글 작성 API",
            description = """
			- Description : 이 API는 댓글을 작성합니다.
			- Assignee : 이신행
		""")
    @ApiResponse(
            responseCode = "201",
            content = @Content(schema = @Schema(implementation = CommentPersistResponse.class)))
    public ResponseEntity<CommentPersistResponse> create(
            @PathVariable("postId") Long postId, @RequestBody CommentCreateRequest request) {
        return ResponseEntity.status(CREATED).body(commentService.create(postId, request));
    }

    @PatchMapping("/{id}")
    @Operation(
            summary = "댓글 수정 API",
            description = """
			- Description : 이 API는 댓글을 수정합니다.
			- Assignee : 이신행
		""")
    @ApiResponse(
            responseCode = "200",
            content = @Content(schema = @Schema(implementation = CommentPersistResponse.class)))
    public CommentPersistResponse update(
            @PathVariable("id") Long id, @RequestBody CommentUpdateRequest request) {
        return commentService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "댓글 삭제 API",
            description = """
			- Description : 이 API는 댓글을 삭제합니다.
			- Assignee : 이신행
		""")
    @ApiResponse(responseCode = "204")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        commentService.delete(id);
        return ResponseEntity.status(NO_CONTENT).build();
    }

    @GetMapping("/{postId}")
    @Operation(
            summary = "댓글 조회 API",
            description = """
			- Description : 이 API는 게시글 별 댓글을 조회합니다.
			- Assignee : 이신행
		""")
    @ApiResponse(
            responseCode = "200",
            content = @Content(schema = @Schema(implementation = CommentPageResponseItem.class)))
    public CustomPageResponse<CommentPageResponseItem> findByPostId(
            @PathVariable("postId") Long postId, @ModelAttribute CustomPageRequest request) {
        return CustomPageResponse.from(commentService.findByPostId(postId, request));
    }

    @PostMapping("/like")
    @Operation(
            summary = "댓글 좋아요 API",
            description =
                    """
			- Description : 이 API는 댓글에 좋아요 상태를 토글합니다.
			- Assignee : 이신행
		""")
    @ApiResponse(responseCode = "201")
    public ResponseEntity<Void> toggleCommentLike(@RequestBody CommentLikeToggleRequest request) {
        commentService.toggleLike(request);
        return ResponseEntity.status(NO_CONTENT).build();
    }
}
