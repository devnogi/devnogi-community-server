package until.the.eternity.dcs.domain.comment.application;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
		UserSummary user = getCurrentUser();
		Comment comment = commentConverter.fromCreateRequestToComment(request, user.getId(), postId);
		Comment save = commentRepository.save(comment);
		return commentConverter.fromCommentToPersistResponse(save);
	}

	@Transactional
	public CommentPersistResponse update(Long id, CommentUpdateRequest request) {
		UserSummary user = getCurrentUser();
		Comment comment = findById(id);
		isCurrentUserEqualsWriter(user.getId(), comment);

		comment.update(request.content(), user.getId());
		return commentConverter.fromCommentToPersistResponse(comment);
	}

	@Transactional
	public void delete(Long id) {
		UserSummary user = getCurrentUser();
		Comment comment = findById(id);
		isCurrentUserEqualsWriter(user.getId(), comment);
		comment.delete(user.getId());
	}

	public Page<CommentPageResponseItem> findByPostId(Long postId, CustomPageRequest request) {
		Pageable pageable = request.toPageable();
		Page<Comment> comments = commentRepository.findByPost(postId, pageable);
		return comments.map(commentConverter::fromCommentToPageResponse);
	}

	private UserSummary getCurrentUser() {
		return userService.getCurrentUser();
	}

	private void isCurrentUserEqualsWriter(Long currentUserId, Comment comment) {
		if (!currentUserId.equals(comment.getUserId())) {
			throw new RuntimeException("Current user is not the same as the user");
		}
	}

	private Comment findById(Long id) {
		return commentRepository.findById(id)
			.orElseThrow(() -> new RuntimeException("Comment not found"));
	}
}
