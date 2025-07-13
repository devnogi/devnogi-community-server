package until.the.eternity.dcs.domain.comment.application;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import until.the.eternity.dcs.common.request.CustomPageRequest;
import until.the.eternity.dcs.domain.comment.dto.request.CommentCreateRequest;
import until.the.eternity.dcs.domain.comment.dto.request.CommentLikeToggleRequest;
import until.the.eternity.dcs.domain.comment.dto.request.CommentUpdateRequest;
import until.the.eternity.dcs.domain.comment.dto.response.CommentPageResponseItem;
import until.the.eternity.dcs.domain.comment.dto.response.CommentPersistResponse;
import until.the.eternity.dcs.domain.comment.entity.Comment;
import until.the.eternity.dcs.domain.comment.entity.CommentLike;
import until.the.eternity.dcs.domain.comment.entity.CommentLikeRepository;
import until.the.eternity.dcs.domain.comment.entity.CommentRepository;
import until.the.eternity.dcs.domain.comment.exception.CommentModifyForbiddenException;
import until.the.eternity.dcs.domain.comment.exception.CommentNotFoundException;
import until.the.eternity.dcs.domain.user.application.UserService;
import until.the.eternity.dcs.domain.user.entity.UserSummary;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CommentService {
	private final CommentRepository commentRepository;
	private final CommentConverter commentConverter;
	private final UserService userService;
	private final CommentLikeRepository commentLikeRepository;
	private final CommentLikeConverter commentLikeConverter;

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

		if (userService.isAuthenticated()) {
			UserSummary user = getCurrentUser();
			Long userId = user.getId();
			List<Long> commentIds = comments.map(Comment::getId).toList();
			Set<Long> likedCommentIds = commentLikeRepository.findIdsByUserIdAndCommentIdIn(userId, commentIds);

			return comments.map(c ->
				commentConverter.fromCommentToPageResponse(c, likedCommentIds.contains(c.getId()))
			);
		}

		return comments.map(commentConverter::fromCommentToPageResponseNonAuth);
	}

	@Transactional
	public void toggleLike(CommentLikeToggleRequest request) {
		Long userId = getCurrentUser().getId();

		if (commentLikeRepository.findByCommentIdAndUserId(request.commentId(), userId).isEmpty()) {
			likeComment(request, userId);
			return;
		}
		unlikeComment(request, userId);
	}

	private void likeComment(CommentLikeToggleRequest request, Long userId) {
		CommentLike commentLike = commentLikeConverter.fromToggleRequest(request, userId);
		commentLikeRepository.save(commentLike);
		Comment comment = findById(request.commentId());
		comment.like();
	}

	private void unlikeComment(CommentLikeToggleRequest request, Long userId) {
		commentLikeRepository.deleteByCommentIdAndUserId(request.commentId(), userId);
		Comment comment = findById(request.commentId());
		comment.unlike();
	}

	private UserSummary getCurrentUser() {
		return userService.getCurrentUser();
	}

	private void isCurrentUserEqualsWriter(Long currentUserId, Comment comment) {
		if (!currentUserId.equals(comment.getUserId())) {
			throw new CommentModifyForbiddenException();
		}
	}

	private Comment findById(Long id) {
		return commentRepository.findById(id)
			.orElseThrow(() -> new CommentNotFoundException(id));
	}
}
