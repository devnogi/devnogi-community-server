package until.the.eternity.dcs.domain.comment.presentation;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import until.the.eternity.dcs.common.request.CustomPageRequest;
import until.the.eternity.dcs.common.response.CustomPageResponse;
import until.the.eternity.dcs.domain.comment.application.CommentService;
import until.the.eternity.dcs.domain.comment.dto.request.CommentCreateRequest;
import until.the.eternity.dcs.domain.comment.dto.request.CommentUpdateRequest;
import until.the.eternity.dcs.domain.comment.dto.response.CommentPageResponseItem;
import until.the.eternity.dcs.domain.comment.dto.response.CommentPersistResponse;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/comment")
public class CommentController {
	private final CommentService commentService;

	// todo 스웨거

	@PostMapping
	public ResponseEntity<CommentPersistResponse> create(@RequestBody CommentCreateRequest request) {
		return ResponseEntity.status(CREATED).body(commentService.create(request));
	}

	@PatchMapping("/{id}")
	public CommentPersistResponse update(
		@PathVariable("id") Long id,
		@RequestBody CommentUpdateRequest request
	) {
		return commentService.update(id, request);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
		commentService.delete(id);
		return ResponseEntity.status(NO_CONTENT).build();
	}

	@GetMapping("/{postId}")
	public CustomPageResponse<CommentPageResponseItem> findByPostId(
		@PathVariable("postId") Long postId,
		@RequestParam CustomPageRequest request
	) {
		return CustomPageResponse.from(commentService.findByPostId(postId, request));
	}

}
