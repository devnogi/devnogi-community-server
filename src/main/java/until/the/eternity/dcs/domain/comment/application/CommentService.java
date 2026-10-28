package until.the.eternity.dcs.domain.comment.application;

import static until.the.eternity.dcs.domain.notice.enums.NoticeType.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import until.the.eternity.dcs.common.entity.CustomWebAuthenticationDetails;
import until.the.eternity.dcs.common.notification.RedisSender;
import until.the.eternity.dcs.common.notification.dto.NotificationJob;
import until.the.eternity.dcs.common.request.CustomPageRequest;
import until.the.eternity.dcs.domain.comment.dto.request.CommentCreateRequest;
import until.the.eternity.dcs.domain.comment.dto.request.CommentLikeToggleRequest;
import until.the.eternity.dcs.domain.comment.dto.request.CommentUpdateRequest;
import until.the.eternity.dcs.domain.comment.dto.response.CommentPageResponseItem;
import until.the.eternity.dcs.domain.comment.dto.response.CommentPersistResponse;
import until.the.eternity.dcs.domain.comment.entity.Comment;
import until.the.eternity.dcs.domain.comment.entity.CommentLike;
import until.the.eternity.dcs.domain.comment.entity.CommentMeta;
import until.the.eternity.dcs.domain.comment.exception.CommentNotFoundException;
import until.the.eternity.dcs.domain.comment.exception.CommentNotLikedYetException;
import until.the.eternity.dcs.domain.comment.infrastructure.CommentLikeRepository;
import until.the.eternity.dcs.domain.comment.infrastructure.CommentMetaRepository;
import until.the.eternity.dcs.domain.comment.infrastructure.CommentRepository;
import until.the.eternity.dcs.domain.post.application.PostMetaService;
import until.the.eternity.dcs.domain.post.entity.Post;
import until.the.eternity.dcs.domain.post.exception.PostNotFoundException;
import until.the.eternity.dcs.domain.post.infrastructure.PostMetaRepository;
import until.the.eternity.dcs.domain.post.infrastructure.PostRepository;
import until.the.eternity.dcs.domain.user.application.UserSummaryService;
import until.the.eternity.dcs.domain.user.entity.UserSummary;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final CommentConverter commentConverter;
    private final CommentLikeRepository commentLikeRepository;
    private final CommentLikeConverter commentLikeConverter;
    private final CommentMetaRepository commentMetaRepository;
    private final PostRepository postRepository;
    private final PostMetaRepository postMetaRepository;
    private final RedisSender redisSender;
    private final CommentPermissionEvaluator commentPermissionEvaluator;
    private final UserSummaryService userSummaryService;
    private final PostMetaService postMetaService;

    @Transactional
    @PreAuthorize("@commentPermissionEvaluator.canCreate(authentication)")
    public CommentPersistResponse create(Long postId, CommentCreateRequest request) {
        Long userId = getCurrentUserId();
        Comment comment = commentConverter.fromCreateRequestToComment(request, userId, postId);
        Comment save = commentRepository.save(comment);
        connectCommentWithPost(postId, save);

        sendCommentCreatedNotice(postId, request.parentComment());

        return commentConverter.fromCommentToPersistResponse(save);
    }

    @Transactional
    @PreAuthorize("@commentPermissionEvaluator.canUpdate(authentication,#id)")
    public CommentPersistResponse update(Long id, CommentUpdateRequest request) {
        Long userId = getCurrentUserId();
        Comment comment = findById(id);
        comment.update(request.content(), userId);
        return commentConverter.fromCommentToPersistResponse(comment);
    }

    @Transactional
    @PreAuthorize("@commentPermissionEvaluator.canDelete(authentication,#id)")
    public void delete(Long id) {
        Long userId = getCurrentUserId();
        Comment comment = findById(id);
        comment.delete(userId);

        disconnectCommentWithPost(comment);
    }

    @Transactional(readOnly = true)
    public Page<CommentPageResponseItem> findByPostId(Long postId, CustomPageRequest request) {
        Pageable pageable = request.toPageable();
        Page<Comment> comments = commentRepository.findAllByPostId(postId, pageable);

        List<Long> commentIds = comments.stream().map(Comment::getId).toList();
        Map<Long, Integer> commentMetaMap =
                commentMetaRepository.findByCommentIdIn(commentIds).stream()
                        .collect(
                                Collectors.toMap(
                                        CommentMeta::getCommentId, CommentMeta::getLikeCount));

        List<Long> userIds = comments.stream().map(Comment::getUserId).toList();
        List<UserSummary> userSummaryList = userSummaryService.findByIdIn(userIds);
        Map<Long, String> userIdToNameMap =
                userSummaryList.stream()
                        .collect(Collectors.toMap(UserSummary::getId, UserSummary::getNickname));

        if (!checkIsAnonymousUser()) {
            Long userId = getCurrentUserId();

            if (userSummaryService.existsUserSummaryById(userId)) {
                Set<Long> likedCommentIds =
                        commentLikeRepository.findIdsByUserIdAndCommentIdIn(userId, commentIds);

                return comments.map(
                        c -> {
                            String username = userIdToNameMap.getOrDefault(c.getUserId(), "익명");
                            return commentConverter.fromCommentToPageResponse(
                                    c,
                                    likedCommentIds.contains(c.getId()),
                                    commentMetaMap.getOrDefault(c.getId(), 0),
                                    username);
                        });
            }
        }
        return comments.map(
                c -> {
                    String username = userIdToNameMap.getOrDefault(c.getUserId(), "익명");
                    return commentConverter.fromCommentToPageResponseNonAuth(
                            c, commentMetaMap.getOrDefault(c.getId(), 0), username);
                });
    }

    @Transactional
    @PreAuthorize("@commentPermissionEvaluator.canToggleLike(authentication)")
    public void toggleLike(CommentLikeToggleRequest request) {
        Long userId = getCurrentUserId();

        if (commentLikeRepository.findByCommentIdAndUserId(request.commentId(), userId).isEmpty()) {
            likeComment(request, userId);

            redisSender.enqueue(NotificationJob.of(userId, COMMENT_LIKE, request.commentId()));

            return;
        }
        unlikeComment(request, userId);
    }

    private void connectCommentWithPost(Long postId, Comment save) {
        Post post = findPostById(postId);
        post.getComments().add(save);
        postRepository.save(post);

        postMetaService.addComment(postId);
    }

    private void disconnectCommentWithPost(Comment comment) {
        Long postId = comment.getPost().getId();
        Post post = findPostById(postId);
        post.getComments().remove(comment);
        comment.disconnectWithPost();
        postMetaService.deleteComment(postId);
    }

    private void sendCommentCreatedNotice(Long postId, Long parentId) {
        if (parentId != null) {
            Comment parent =
                    commentRepository
                            .findById(parentId)
                            .orElseThrow(() -> new CommentNotFoundException(parentId));
            redisSender.enqueue(NotificationJob.of(parent.getUserId(), COMMENT_REPLY, parentId));
        } else {
            Post post = findPostById(postId);
            redisSender.enqueue(NotificationJob.of(post.getUserId(), POST_COMMENT, postId));
        }
    }

    private Post findPostById(Long postId) {
        return postRepository
                .findByIdAndIsDeletedFalseAndIsBlockedFalse(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));
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

    private Comment findById(Long id) {
        return commentRepository.findById(id).orElseThrow(() -> new CommentNotFoundException(id));
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return commentPermissionEvaluator.getCurrentUserId(auth);
    }

    private boolean checkIsAnonymousUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return commentPermissionEvaluator.isAnonymousUser(auth);
    }

    private String getCurrentUserIp() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        Object details = auth.getDetails();
        if (details instanceof CustomWebAuthenticationDetails webDetails) {
            return webDetails.getRealRemoteAddress();
        }

        return "UNKNOWN_IP";
    }
}
