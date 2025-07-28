package until.the.eternity.dcs.domain.comment.application;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
import until.the.eternity.dcs.domain.comment.entity.CommentMeta;
import until.the.eternity.dcs.domain.comment.entity.CommentMetaRepository;
import until.the.eternity.dcs.domain.comment.entity.CommentRepository;
import until.the.eternity.dcs.domain.comment.exception.CommentModifyForbiddenException;
import until.the.eternity.dcs.domain.comment.exception.CommentNotFoundException;
import until.the.eternity.dcs.domain.comment.exception.CommentNotLikedYetException;
import until.the.eternity.dcs.domain.post.entity.Post;
import until.the.eternity.dcs.domain.post.entity.PostMeta;
import until.the.eternity.dcs.domain.post.exception.PostNotFoundException;
import until.the.eternity.dcs.domain.post.infrastructure.PostMetaRepository;
import until.the.eternity.dcs.domain.post.infrastructure.PostRepository;
import until.the.eternity.dcs.domain.user.application.UserService;
import until.the.eternity.dcs.domain.user.entity.UserSummary;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final CommentConverter commentConverter;
    private final UserService userService;
    private final CommentLikeRepository commentLikeRepository;
    private final CommentLikeConverter commentLikeConverter;
    private final CommentMetaRepository commentMetaRepository;
    private final PostRepository postRepository;
    private final PostMetaRepository postMetaRepository;

    @Transactional
    public CommentPersistResponse create(Long postId, CommentCreateRequest request) {
        UserSummary user = getCurrentUser();
        Comment comment =
                commentConverter.fromCreateRequestToComment(request, user.getId(), postId);
        Comment save = commentRepository.save(comment);

        connectCommentWithPost(postId, save);

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

        disconnectCommentWithPost(comment);
    }

    @Transactional(readOnly = true)
    public Page<CommentPageResponseItem> findByPostId(Long postId, CustomPageRequest request) {
        Pageable pageable = request.toPageable();

        Page<Comment> comments = commentRepository.findByPost(postId, pageable);

        // todo 추후 join 으로 조회
        Map<Long, Integer> commentMetaMap = new HashMap<>();
        for (Comment comment : comments) {
            Optional<CommentMeta> commentMeta = commentMetaRepository.findById(comment.getId());
            if (commentMeta.isPresent()) {
                commentMetaMap.put(comment.getId(), commentMeta.get().getLikeCount());
            } else {
                commentMetaMap.put(comment.getId(), 0);
            }
        }

        if (userService.isAuthenticated()) {
            UserSummary user = getCurrentUser();
            List<Long> commentIds = comments.map(Comment::getId).toList();
            Set<Long> likedCommentIds =
                    commentLikeRepository.findIdsByUserIdAndCommentIdIn(user.getId(), commentIds);

            return comments.map(
                    c ->
                            commentConverter.fromCommentToPageResponse(
                                    c,
                                    likedCommentIds.contains(c.getId()),
                                    commentMetaMap.get(c.getId())));
        }

        return comments.map(
                c ->
                        commentConverter.fromCommentToPageResponseNonAuth(
                                c, commentMetaMap.get(c.getId())));
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

    private void connectCommentWithPost(Long postId, Comment save) {
        Post post = findPostById(postId);
        post.getComments().add(save);
        postRepository.save(post);

        PostMeta postMeta = findPostMetaById(postId);
        postMeta.addComment();
        postMetaRepository.save(postMeta);
    }

    private void disconnectCommentWithPost(Comment comment) {
        Long postId = comment.getPost().getId();

        Post post = findPostById(postId);
        post.getComments().remove(comment);

        PostMeta postMeta = findPostMetaById(postId);
        postMeta.deleteComment();
    }

    private Post findPostById(Long postId) {
        return postRepository
                .findByIdAndIsDeletedFalseAndIsBlockedFalse(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));
    }

    private PostMeta findPostMetaById(Long postId) {
        return postMetaRepository.findByPostId(postId).orElse(PostMeta.create(postId));
    }

    private void likeComment(CommentLikeToggleRequest request, Long userId) {
        CommentLike commentLike = commentLikeConverter.fromToggleRequest(request, userId);
        commentLikeRepository.save(commentLike);

        Long commentId = request.commentId();
        CommentMeta commentMeta =
                commentMetaRepository.findById(commentId).orElse(CommentMeta.create(commentId));
        commentMeta.like();
        commentMetaRepository.save(commentMeta);
    }

    private void unlikeComment(CommentLikeToggleRequest request, Long userId) {
        commentLikeRepository.deleteByCommentIdAndUserId(request.commentId(), userId);

        Long commentId = request.commentId();
        CommentMeta commentMeta =
                commentMetaRepository
                        .findById(commentId)
                        .orElseThrow(() -> new CommentNotLikedYetException(commentId));
        commentMeta.unlike();
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
        return commentRepository.findById(id).orElseThrow(() -> new CommentNotFoundException(id));
    }
}
