package until.the.eternity.dcs.domain.comment.application;

import static until.the.eternity.dcs.domain.notice.enums.NoticeType.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import until.the.eternity.dcs.common.notification.RedisSender;
import until.the.eternity.dcs.common.notification.dto.NotificationJob;
import until.the.eternity.dcs.common.request.CustomPageRequest;
import until.the.eternity.dcs.domain.comment.dto.request.CommentCreateRequest;
import until.the.eternity.dcs.domain.comment.dto.request.CommentLikeToggleRequest;
import until.the.eternity.dcs.domain.comment.dto.request.CommentUpdateRequest;
import until.the.eternity.dcs.domain.comment.dto.request.UserCommentPageRequest;
import until.the.eternity.dcs.domain.comment.dto.response.CommentPageResponseItem;
import until.the.eternity.dcs.domain.comment.dto.response.CommentPersistResponse;
import until.the.eternity.dcs.domain.comment.dto.response.CommentThreadResponse;
import until.the.eternity.dcs.domain.comment.dto.response.UserCommentActivityResponse;
import until.the.eternity.dcs.domain.comment.entity.Comment;
import until.the.eternity.dcs.domain.comment.entity.CommentLike;
import until.the.eternity.dcs.domain.comment.entity.CommentMeta;
import until.the.eternity.dcs.domain.comment.exception.CommentNotFoundException;
import until.the.eternity.dcs.domain.comment.exception.CommentNotLikedYetException;
import until.the.eternity.dcs.domain.comment.infrastructure.CommentLikeRepository;
import until.the.eternity.dcs.domain.comment.infrastructure.CommentMetaRepository;
import until.the.eternity.dcs.domain.comment.infrastructure.CommentReplyCount;
import until.the.eternity.dcs.domain.comment.infrastructure.CommentRepository;
import until.the.eternity.dcs.domain.experience.ActivityOutbox;
import until.the.eternity.dcs.domain.experience.ActivitySource;
import until.the.eternity.dcs.domain.post.application.PostMetaService;
import until.the.eternity.dcs.domain.post.entity.Post;
import until.the.eternity.dcs.domain.post.exception.PostNotFoundException;
import until.the.eternity.dcs.domain.post.infrastructure.PostRepository;
import until.the.eternity.dcs.domain.user.application.UserSummaryService;
import until.the.eternity.dcs.domain.user.entity.UserSummary;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final ActivityOutbox activityOutbox;
    private final CommentConverter commentConverter;
    private final CommentLikeRepository commentLikeRepository;
    private final CommentLikeConverter commentLikeConverter;
    private final CommentMetaRepository commentMetaRepository;
    private final PostRepository postRepository;
    private final RedisSender redisSender;
    private final CommentPermissionEvaluator commentPermissionEvaluator;
    private final UserSummaryService userSummaryService;
    private final PostMetaService postMetaService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    @PreAuthorize("@commentPermissionEvaluator.canCreate(authentication)")
    public CommentPersistResponse create(Long postId, CommentCreateRequest request) {
        Long userId = getCurrentUserId();
        activityOutbox.lockPost(postId);
        Comment comment = commentConverter.fromCreateRequestToComment(request, userId, postId);
        Comment save = commentRepository.save(comment);
        connectCommentWithPost(postId, save);
        if (!Boolean.TRUE.equals(save.getPost().getIsDraft()))
            activityOutbox.created(ActivitySource.COMMENT, save.getId(), save.getUserId(), postId);

        eventPublisher.publishEvent(
                createCommentCreatedNotification(postId, request.parentComment()));

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

    @Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    @PreAuthorize("@commentPermissionEvaluator.canDelete(authentication,#id)")
    public void delete(Long id) {
        Long userId = getCurrentUserId();
        activityOutbox.lockCommentRoot(id);
        Comment comment = findById(id);
        activityOutbox.deletedComment(id);
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
        Map<Long, UserSummary> userSummaries =
                userSummaryList.stream()
                        .collect(Collectors.toMap(UserSummary::getId, summary -> summary));

        if (!checkIsAnonymousUser()) {
            Long userId = getCurrentUserId();

            if (userSummaryService.existsUserSummaryById(userId)) {
                Set<Long> likedCommentIds =
                        commentLikeRepository.findIdsByUserIdAndCommentIdIn(userId, commentIds);

                return comments.map(
                        c -> {
                            UserSummary author = userSummaries.get(c.getUserId());
                            return commentConverter.fromCommentToPageResponse(
                                    c,
                                    likedCommentIds.contains(c.getId()),
                                    commentMetaMap.getOrDefault(c.getId(), 0),
                                    author == null ? "익명" : author.getNickname(),
                                    author == null ? null : author.getProfileImageUrl(),
                                    author == null ? null : author.getServerName(),
                                    author != null && author.isVerified(),
                                    author == null ? null : author.getActivityGrade(),
                                    author == null ? null : author.getArcana());
                        });
            }
        }
        return comments.map(
                c -> {
                    UserSummary author = userSummaries.get(c.getUserId());
                    return commentConverter.fromCommentToPageResponseNonAuth(
                            c,
                            commentMetaMap.getOrDefault(c.getId(), 0),
                            author == null ? "익명" : author.getNickname(),
                            author == null ? null : author.getProfileImageUrl(),
                            author == null ? null : author.getServerName(),
                            author != null && author.isVerified(),
                            author == null ? null : author.getActivityGrade(),
                            author == null ? null : author.getArcana());
                });
    }

    @Transactional(readOnly = true)
    public Page<CommentThreadResponse> findThreads(Long postId, CustomPageRequest request) {
        Pageable requested = request.toPageable();
        Pageable pageable =
                org.springframework.data.domain.PageRequest.of(
                        requested.getPageNumber(),
                        Math.min(requested.getPageSize(), 20),
                        org.springframework.data.domain.Sort.by("createdAt").descending());
        Page<Comment> parents =
                commentRepository.findAllByPostIdAndParentCommentIsNull(postId, pageable);
        List<Long> parentIds = parents.stream().map(Comment::getId).toList();
        List<Long> replyIds =
                parentIds.isEmpty()
                        ? List.of()
                        : commentRepository.findFirst3ReplyIdsByPostIdAndParentIds(
                                postId, parentIds);
        List<Comment> allReplies =
                replyIds.isEmpty() ? List.of() : commentRepository.findAllByIdIn(replyIds);
        allReplies =
                allReplies.stream()
                        .sorted(
                                java.util.Comparator.comparing(
                                                (Comment reply) -> reply.getParentComment().getId())
                                        .thenComparing(
                                                Comment::getCreatedAt,
                                                java.util.Comparator.nullsFirst(
                                                        java.util.Comparator.naturalOrder()))
                                        .thenComparing(Comment::getId))
                        .toList();
        Map<Long, Long> replyCounts =
                parentIds.isEmpty()
                        ? Map.of()
                        : commentRepository
                                .countRepliesByPostIdAndParentIds(postId, parentIds)
                                .stream()
                                .collect(
                                        Collectors.toMap(
                                                CommentReplyCount::getParentId,
                                                CommentReplyCount::getReplyCount));
        List<Comment> allComments =
                java.util.stream.Stream.concat(parents.stream(), allReplies.stream()).toList();
        Map<Long, CommentPageResponseItem> rendered = renderComments(allComments);
        Map<Long, List<Comment>> repliesByParent =
                allReplies.stream()
                        .collect(Collectors.groupingBy(reply -> reply.getParentComment().getId()));
        return parents.map(
                parent -> {
                    List<Comment> replies = repliesByParent.getOrDefault(parent.getId(), List.of());
                    return new CommentThreadResponse(
                            rendered.get(parent.getId()),
                            replies.stream()
                                    .limit(3)
                                    .map(reply -> rendered.get(reply.getId()))
                                    .toList(),
                            replyCounts.getOrDefault(parent.getId(), 0L));
                });
    }

    @Transactional(readOnly = true)
    public Page<CommentPageResponseItem> findReplies(
            Long postId, Long parentId, CustomPageRequest request) {
        Comment parent = findById(parentId);
        if (parent.getParentComment() != null
                || parent.getPost() == null
                || !parent.getPost().getId().equals(postId)) {
            throw new CommentNotFoundException(parentId);
        }
        Pageable requested = request.toPageable();
        Pageable oldestFirst =
                org.springframework.data.domain.PageRequest.of(
                        requested.getPageNumber(),
                        requested.getPageSize(),
                        org.springframework.data.domain.Sort.by("createdAt").ascending());
        Page<Comment> replies =
                commentRepository.findAllByPostIdAndParentCommentId(postId, parentId, oldestFirst);
        Map<Long, CommentPageResponseItem> rendered = renderComments(replies.getContent());
        return replies.map(reply -> rendered.get(reply.getId()));
    }

    private Map<Long, CommentPageResponseItem> renderComments(List<Comment> comments) {
        if (comments.isEmpty()) {
            return Map.of();
        }
        List<Long> commentIds = comments.stream().map(Comment::getId).toList();
        Map<Long, Integer> likeCounts =
                commentMetaRepository.findByCommentIdIn(commentIds).stream()
                        .collect(
                                Collectors.toMap(
                                        CommentMeta::getCommentId, CommentMeta::getLikeCount));
        Map<Long, UserSummary> userSummaries =
                userSummaryService
                        .findByIdIn(comments.stream().map(Comment::getUserId).distinct().toList())
                        .stream()
                        .collect(Collectors.toMap(UserSummary::getId, summary -> summary));
        Long currentUserId = optionalCurrentUserId();
        Set<Long> likedIds =
                currentUserId == null
                        ? Set.of()
                        : commentLikeRepository.findIdsByUserIdAndCommentIdIn(
                                currentUserId, commentIds);
        return comments.stream()
                .collect(
                        Collectors.toMap(
                                Comment::getId,
                                comment ->
                                        renderComment(
                                                comment,
                                                likedIds.contains(comment.getId()),
                                                likeCounts.getOrDefault(comment.getId(), 0),
                                                userSummaries.get(comment.getUserId()))));
    }

    private Long optionalCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null
                || !auth.isAuthenticated()
                || !(auth.getPrincipal() instanceof Long userId)) {
            return null;
        }
        return userId;
    }

    @Transactional(readOnly = true)
    public Page<UserCommentActivityResponse> findByUserId(
            Long userId, UserCommentPageRequest request) {
        Page<Comment> comments =
                commentRepository.findVisibleByUserId(userId, request.toPageable());
        List<Long> commentIds = comments.stream().map(Comment::getId).toList();
        Map<Long, Integer> likeCounts =
                commentMetaRepository.findByCommentIdIn(commentIds).stream()
                        .collect(
                                Collectors.toMap(
                                        CommentMeta::getCommentId, CommentMeta::getLikeCount));
        UserSummary author =
                userSummaryService.findByIdIn(List.of(userId)).stream().findFirst().orElse(null);

        return comments.map(
                comment ->
                        UserCommentActivityResponse.of(
                                comment,
                                likeCounts.getOrDefault(comment.getId(), 0),
                                author == null ? null : author.getServerName(),
                                author != null && author.isVerified(),
                                author == null ? null : author.getActivityGrade(),
                                author == null ? null : author.getArcana()));
    }

    private CommentPageResponseItem renderComment(
            Comment comment, boolean isLiked, int likeCount, UserSummary author) {
        return commentConverter.fromCommentToPageResponse(
                comment,
                isLiked,
                likeCount,
                author == null ? "익명" : author.getNickname(),
                author == null ? null : author.getProfileImageUrl(),
                author == null ? null : author.getServerName(),
                author != null && author.isVerified(),
                author == null ? null : author.getActivityGrade(),
                author == null ? null : author.getArcana());
    }

    @Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    @PreAuthorize("@commentPermissionEvaluator.canToggleLike(authentication)")
    public void toggleLike(CommentLikeToggleRequest request) {
        Long userId = getCurrentUserId();
        long root = activityOutbox.lockCommentRoot(request.commentId());
        Comment target = findById(request.commentId());
        if (commentLikeRepository.findByCommentIdAndUserId(request.commentId(), userId).isEmpty()) {
            likeComment(request, userId);
            if (!Boolean.TRUE.equals(target.getPost().getIsDraft()))
                activityOutbox.changed(
                        ActivitySource.COMMENT_LIKE,
                        request.commentId(),
                        userId,
                        target.getUserId(),
                        root,
                        true);

            redisSender.enqueue(NotificationJob.of(userId, COMMENT_LIKE, request.commentId()));

            return;
        }
        unlikeComment(request, userId);
        activityOutbox.changed(
                ActivitySource.COMMENT_LIKE,
                request.commentId(),
                userId,
                target.getUserId(),
                root,
                false);
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

    private CommentCreatedNotification createCommentCreatedNotification(
            Long postId, Long parentId) {
        if (parentId != null) {
            Comment parent =
                    commentRepository
                            .findById(parentId)
                            .orElseThrow(() -> new CommentNotFoundException(parentId));
            return new CommentCreatedNotification(
                    NotificationJob.of(parent.getUserId(), COMMENT_REPLY, parentId));
        }
        Post post = findPostById(postId);
        return new CommentCreatedNotification(
                NotificationJob.of(post.getUserId(), POST_COMMENT, postId));
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
}
