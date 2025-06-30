package until.the.eternity.dcs.domain.comment.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import until.the.eternity.dcs.domain.comment.dto.request.CommentCreateRequest;
import until.the.eternity.dcs.domain.comment.dto.response.CommentPageResponseItem;
import until.the.eternity.dcs.domain.comment.dto.response.CommentPersistResponse;
import until.the.eternity.dcs.domain.comment.entity.Comment;
import until.the.eternity.dcs.domain.comment.entity.CommentRepository;
import until.the.eternity.dcs.domain.post.entity.Post;

@Component
@RequiredArgsConstructor
public class CommentConverter {
	private final CommentRepository commentRepository;
	// todo PostService 구현 후 post 조회

	public Comment fromCreateRequestToComment(CommentCreateRequest request, Long userId, Long postId) {
		Post post = Post.builder().id(postId).build();
		if(request.parentComment() == null) {
			return Comment.builder()
				.post(post)
				.userId(userId)
				.content(request.content())
				.build();
		}

		Comment parentComment = commentRepository.findById(request.parentComment())
			.orElseThrow(() -> new RuntimeException("Comment not found"));

		return Comment.builder()
			.post(post)
			.userId(userId)
			.parentComment(parentComment)
			.content(request.content())
			.build();
	}

	public CommentPersistResponse fromCommentToPersistResponse(Comment comment) {
		return CommentPersistResponse.from(comment);
	}

	public CommentPageResponseItem fromCommentToPageResponse(Comment comment) {
		return CommentPageResponseItem.from(comment);
	}
}
