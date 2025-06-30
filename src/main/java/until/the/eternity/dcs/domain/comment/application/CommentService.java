package until.the.eternity.dcs.domain.comment.application;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import until.the.eternity.dcs.common.request.CustomPageRequest;
import until.the.eternity.dcs.domain.comment.dto.request.CommentCreateRequest;
import until.the.eternity.dcs.domain.comment.dto.request.CommentUpdateRequest;
import until.the.eternity.dcs.domain.comment.dto.response.CommentPageResponseItem;
import until.the.eternity.dcs.domain.comment.dto.response.CommentPersistResponse;
import until.the.eternity.dcs.domain.comment.entity.Comment;
import until.the.eternity.dcs.domain.comment.entity.CommentRepository;
import until.the.eternity.dcs.domain.user.application.UserService;
import until.the.eternity.dcs.domain.user.entity.UserSummary;

@Service
@RequiredArgsConstructor
public class CommentService {
	private final CommentRepository commentRepository;
	private final CommentConverter commentConverter;
	private final UserService userService;

	public CommentPersistResponse create(Long postId, CommentCreateRequest request) {
		UserSummary user = userService.getCurrentUser();
		Comment comment = commentConverter.fromCreateRequestToComment(request, user.getId(), postId);
		Comment save = commentRepository.save(comment);
		return commentConverter.fromCommentToPersistResponse(save);
	}

	public CommentPersistResponse update(Long id, CommentUpdateRequest request) {
		return null;
	}

	public void delete(Long id) {

	}

	public Page<CommentPageResponseItem> findByPostId(Long postId, CustomPageRequest request) {
		return null;
	}

	public Comment findById(Long id) {
		return commentRepository.findById(id)
			.orElseThrow(() -> new RuntimeException("Comment not found"));
	}
}
