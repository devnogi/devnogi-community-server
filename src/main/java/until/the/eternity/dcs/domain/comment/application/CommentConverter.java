package until.the.eternity.dcs.domain.comment.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import until.the.eternity.dcs.domain.comment.dto.request.CommentCreateRequest;
import until.the.eternity.dcs.domain.comment.dto.response.CommentPageResponseItem;
import until.the.eternity.dcs.domain.comment.dto.response.CommentPersistResponse;
import until.the.eternity.dcs.domain.comment.entity.Comment;
import until.the.eternity.dcs.domain.comment.entity.CommentRepository;
import until.the.eternity.dcs.domain.comment.exception.CommentNotFoundException;
import until.the.eternity.dcs.domain.post.entity.Post;
import until.the.eternity.dcs.domain.post.exception.PostNotFoundException;
import until.the.eternity.dcs.domain.post.infrastructure.PostRepository;

@Component
@RequiredArgsConstructor
public class CommentConverter {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    public Comment fromCreateRequestToComment(
            CommentCreateRequest request, Long userId, Long postId) {
        Post post =
                postRepository
                        .findByIdAndIsDeletedFalseAndIsBlockedFalse(postId)
                        .orElseThrow(() -> new PostNotFoundException(postId));

        Long parentId = request.parentComment();
        if (parentId == null) {
            return Comment.builder().post(post).userId(userId).content(request.content()).build();
        }

        Comment parentComment =
                commentRepository
                        .findById(parentId)
                        .orElseThrow(() -> new CommentNotFoundException(parentId));

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

    public CommentPageResponseItem fromCommentToPageResponse(
            Comment comment, Boolean isLiked, Integer likeCount) {
        return CommentPageResponseItem.from(comment, isLiked, likeCount);
    }

    public CommentPageResponseItem fromCommentToPageResponseNonAuth(
            Comment comment, Integer likeCount) {
        return CommentPageResponseItem.from(comment, false, likeCount);
    }
}
