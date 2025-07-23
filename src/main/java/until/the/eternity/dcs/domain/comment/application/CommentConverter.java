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

@Component
@RequiredArgsConstructor
public class CommentConverter {
    private final CommentRepository commentRepository;

    public Comment fromCreateRequestToComment(
            CommentCreateRequest request, Long userId, Long postId) {
        // todo PostService 구현 후 post 조회
        Post post = Post.builder().id(postId).build();
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
