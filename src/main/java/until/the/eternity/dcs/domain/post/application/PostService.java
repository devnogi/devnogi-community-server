package until.the.eternity.dcs.domain.post.application;

import static until.the.eternity.dcs.domain.notice.enums.NoticeType.POST_LIKE;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import until.the.eternity.dcs.common.entity.CustomWebAuthenticationDetails;
import until.the.eternity.dcs.common.infrastructure.MinioService;
import until.the.eternity.dcs.common.notification.RedisSender;
import until.the.eternity.dcs.common.notification.dto.NotificationJob;
import until.the.eternity.dcs.domain.board.application.BoardService;
import until.the.eternity.dcs.domain.board.entity.Board;
import until.the.eternity.dcs.domain.comment.entity.Comment;
import until.the.eternity.dcs.domain.experience.ActivityOutbox;
import until.the.eternity.dcs.domain.experience.ActivitySource;
import until.the.eternity.dcs.domain.post.dto.request.PostCreateRequest;
import until.the.eternity.dcs.domain.post.dto.request.PostLikeCreateRequest;
import until.the.eternity.dcs.domain.post.dto.request.PostMediaUpdateRequest;
import until.the.eternity.dcs.domain.post.dto.request.PostPageRequest;
import until.the.eternity.dcs.domain.post.dto.request.PostUpdateRequest;
import until.the.eternity.dcs.domain.post.dto.response.PostDetailResponse;
import until.the.eternity.dcs.domain.post.dto.response.PostFeedItem;
import until.the.eternity.dcs.domain.post.dto.response.PostMetaResponse;
import until.the.eternity.dcs.domain.post.dto.response.PostPersistResponse;
import until.the.eternity.dcs.domain.post.dto.response.PostSummaryResponse;
import until.the.eternity.dcs.domain.post.entity.Post;
import until.the.eternity.dcs.domain.post.entity.PostBookmark;
import until.the.eternity.dcs.domain.post.entity.PostImage;
import until.the.eternity.dcs.domain.post.entity.PostLike;
import until.the.eternity.dcs.domain.post.enums.PostMediaType;
import until.the.eternity.dcs.domain.post.enums.PostMetaType;
import until.the.eternity.dcs.domain.post.exception.PostDraftRequiredException;
import until.the.eternity.dcs.domain.post.exception.PostNotFoundException;
import until.the.eternity.dcs.domain.post.infrastructure.PostBookmarkRepository;
import until.the.eternity.dcs.domain.post.infrastructure.PostLikeRepository;
import until.the.eternity.dcs.domain.post.infrastructure.PostRepository;
import until.the.eternity.dcs.domain.tag.application.PostTagService;
import until.the.eternity.dcs.domain.tag.application.TagService;
import until.the.eternity.dcs.domain.tag.entity.PostTag;
import until.the.eternity.dcs.domain.user.application.UserSummaryService;
import until.the.eternity.dcs.domain.user.dto.response.UserSummaryDetailResponse;
import until.the.eternity.dcs.domain.user.entity.UserSummary;
import until.the.eternity.dcs.domain.user.exception.UserNotFoundException;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final ActivityOutbox activityOutbox;
    private final PostConverter postConverter;
    private final PostLikeRepository postLikeRepository;
    private final PostLikeConverter postLikeConverter;
    private final BoardService boardService;
    private final TagService tagService;
    private final PostTagService postTagService;
    private final RedisSender redisSender;
    private final PostMetaService postMetaService;
    private final PostPermissionEvaluator postPermissionEvaluator;
    private final MinioService minioService;
    private final UserSummaryService userSummaryService;
    private final PostBookmarkRepository postBookmarkRepository;

    @Transactional
    @PreAuthorize("@postPermissionEvaluator.canCreate(authentication)")
    public PostPersistResponse createPost(PostCreateRequest request, List<MultipartFile> files) {

        List<String> uploadedFileNames = new ArrayList<>();
        boolean plainText = "PLAIN_TEXT_V1".equals(request.contentFormat());
        if (!plainText && request.isDraft() == null) {
            throw new PostDraftRequiredException();
        }
        if (plainText) {
            validatePlainContent(request.content());
        }

        Long userId = getCurrentUserId();
        Post post = postConverter.fromCreateRequestToPost(request, userId);
        Post savedPost = postRepository.save(post);

        List<PostTag> postTags =
                (plainText
                                ? List.<String>of()
                                : Optional.ofNullable(request.tags()).orElseGet(List::of))
                        .stream()
                                .map(tagService::findOrCreateTag)
                                .map(tag -> PostTag.builder().post(savedPost).tag(tag).build())
                                .collect(Collectors.toList());
        if (files != null && !files.isEmpty()) {
            validateMediaSet(files);
            try {
                for (int i = 0; i < files.size(); i++) {
                    MultipartFile file = files.get(i);
                    PostMediaValidator.ValidatedMedia media = PostMediaValidator.validate(file);
                    String storedFileName = minioService.uploadFile(file);
                    uploadedFileNames.add(storedFileName);
                    savedPost.addMedia(
                            file.getOriginalFilename(),
                            storedFileName,
                            media.type(),
                            media.mimeType(),
                            file.getSize(),
                            i);
                }
            } catch (Exception e) {
                for (String fileName : uploadedFileNames) {
                    try {
                        minioService.deleteFile(fileName);
                    } catch (Exception ignore) {
                        log.error("롤백 중 파일 삭제 실패: {}", fileName);
                    }
                }
                log.error("게시글 생성 실패: 이미지 업로드 중 예외 발생 (userId={})", userId, e);
                throw e;
            }
            registerUploadedMediaRollback(List.copyOf(uploadedFileNames));
        }

        postTagService.savePostTags(postTags);
        postMetaService.createPostMeta(savedPost.getId());
        if (savedPost.markFirstPublication())
            activityOutbox.created(
                    ActivitySource.POST, savedPost.getId(), userId, savedPost.getId());
        return postConverter.fromPostToPostPersistResponse(savedPost);
    }

    public PostDetailResponse findPost(Long id) {
        Post post = findById(id);

        String userIp =
                checkIsAnonymousUser() ? getCurrentUserIp() : String.valueOf(getCurrentUserId());
        List<String> imageUrls =
                post.getImages().stream()
                        .map(image -> minioService.getFileUrl(image.getStoredFileName()))
                        .collect(Collectors.toList());
        List<String> tags =
                Optional.ofNullable(post.getPostTags()).orElseGet(List::of).stream()
                        .map(postTag -> postTag.getTag().getName())
                        .toList();
        String nickname = "알수없음";
        UserSummaryDetailResponse userSummary;
        try {
            userSummary = userSummaryService.findUserSummary(post.getUserId());
            nickname = userSummary.nickname();
        } catch (UserNotFoundException ignore) {
        }

        postMetaService.viewPost(id, userIp);
        PostMetaResponse postMeta = postMetaService.getPostMetaInfo(id);
        Long currentUserId = getOptionalCurrentUserId();
        PostDetailResponse response =
                postConverter.fromPostToPostDetailResponse(
                        post,
                        postMeta,
                        imageUrls,
                        nickname,
                        tags,
                        currentUserId != null
                                && postLikeRepository.existsByUserIdAndPostId(currentUserId, id),
                        currentUserId != null
                                && postBookmarkRepository.existsByUserIdAndPostId(
                                        currentUserId, id));
        UserSummary socialUser =
                userSummaryService.findByIdIn(List.of(post.getUserId())).stream()
                        .findFirst()
                        .orElse(null);
        PostFeedItem.Author author =
                new PostFeedItem.Author(
                        socialUser == null ? nickname : socialUser.getNickname(),
                        socialUser == null ? null : socialUser.getProfileImageUrl(),
                        socialUser == null ? null : socialUser.getServerName(),
                        socialUser != null && socialUser.isVerified(),
                        socialUser == null ? null : socialUser.getActivityGrade());
        List<PostFeedItem.Media> media =
                post.getImages().stream()
                        .sorted(java.util.Comparator.comparingInt(PostImage::getDisplayOrder))
                        .map(
                                item ->
                                        new PostFeedItem.Media(
                                                item.getId(),
                                                item.getMediaType().name(),
                                                minioService.getFileUrl(item.getStoredFileName()),
                                                item.getMimeType(),
                                                item.getDisplayOrder()))
                        .toList();
        return response.withSocial(author, media);
    }

    public Page<PostFeedItem> findFeed(
            PostPageRequest request, String sort, String keyword, Long userId) {
        Pageable pageable = request.toPageable();
        Page<Post> posts;
        if (userId != null) {
            posts =
                    postRepository.findAllByUserIdAndIsDeletedFalseAndIsBlockedFalseAndIsDraftFalse(
                            userId, pageable);
        } else if ("POPULAR".equalsIgnoreCase(sort)) {
            posts = postRepository.findPopularPosts(pageable);
        } else {
            posts = postRepository.findFeed(keyword, pageable);
        }
        return toFeedItems(posts, getOptionalCurrentUserId());
    }

    @PreAuthorize("@postPermissionEvaluator.canTogglePostLike(authentication)")
    public Page<PostFeedItem> findMyFeed(PostPageRequest request) {
        return findFeed(request, "LATEST", null, getCurrentUserId());
    }

    @PreAuthorize("@postPermissionEvaluator.canTogglePostLike(authentication)")
    public Page<PostFeedItem> findBookmarks(PostPageRequest request) {
        Long userId = getCurrentUserId();
        Page<PostBookmark> bookmarks =
                postBookmarkRepository.findVisibleByUserId(
                        userId,
                        PageRequest.of(
                                request.toPageable().getPageNumber(),
                                request.toPageable().getPageSize(),
                                Sort.by(Sort.Direction.DESC, "createdAt")
                                        .and(Sort.by(Sort.Direction.DESC, "id"))));
        List<Post> posts = bookmarks.map(PostBookmark::getPost).getContent();
        Page<Post> postPage =
                new org.springframework.data.domain.PageImpl<>(
                        posts, bookmarks.getPageable(), bookmarks.getTotalElements());
        return toFeedItems(postPage, userId);
    }

    @Transactional
    @PreAuthorize("@postPermissionEvaluator.canTogglePostLike(authentication)")
    public void bookmark(Long postId) {
        Long userId = getCurrentUserId();
        findById(postId);
        postBookmarkRepository.insertIgnore(userId, postId);
    }

    @Transactional
    @PreAuthorize("@postPermissionEvaluator.canTogglePostLike(authentication)")
    public void removeBookmark(Long postId) {
        postBookmarkRepository.deleteByUserIdAndPostId(getCurrentUserId(), postId);
    }

    public Page<PostSummaryResponse> findPosts(PostPageRequest request) {
        Pageable pageable = request.toPageable();

        Page<Post> posts = postRepository.findAllByIsDeletedFalseAndIsBlockedFalse(pageable);
        return getPostSummaryResponses(posts);
    }

    @Transactional
    @PreAuthorize("@postPermissionEvaluator.canUpdate(authentication,#id)")
    public PostPersistResponse updatePost(Long id, PostUpdateRequest postUpdateRequest) {
        Long userId = getCurrentUserId();

        activityOutbox.lockPost(id);
        Post post = findById(id);
        boolean wasDraft = Boolean.TRUE.equals(post.getIsDraft());
        post.preserveLegacyPublication();

        List<String> currentList =
                post.getPostTags().stream().map(postTag -> postTag.getTag().getName()).toList();

        List<String> newTags = postUpdateRequest.tags();

        if (newTags != null) {
            List<PostTag> toDeleteTags =
                    currentList.stream()
                            .filter(name -> !newTags.contains(name))
                            .map(tagService::findOrCreateTag)
                            .map(tag -> PostTag.builder().post(post).tag(tag).build())
                            .toList();

            postTagService.deletePostTags(toDeleteTags);

            List<PostTag> toAddTags =
                    newTags.stream()
                            .filter(name -> !currentList.contains(name))
                            .map(tagService::findOrCreateTag)
                            .map(tag -> PostTag.builder().post(post).tag(tag).build())
                            .toList();

            postTagService.savePostTags(toAddTags);

            post.getPostTags().clear();
        }

        if ("PLAIN_TEXT_V1".equals(postUpdateRequest.contentFormat())) {
            validatePlainContent(postUpdateRequest.content());
            post.updatePlainContent(postUpdateRequest.content(), userId);
        } else {
            post.update(
                    postUpdateRequest.title(),
                    postUpdateRequest.content(),
                    postUpdateRequest.isDraft(),
                    null,
                    userId);
        }

        Post updatedPost = postRepository.save(post);
        if (wasDraft && post.markFirstPublication())
            activityOutbox.created(ActivitySource.POST, id, post.getUserId(), id);

        return postConverter.fromPostToPostPersistResponse(updatedPost);
    }

    @Transactional
    @PreAuthorize("@postPermissionEvaluator.canUpdate(authentication,#id)")
    public PostPersistResponse updatePlainPost(
            Long id, PostMediaUpdateRequest request, List<MultipartFile> newFiles) {
        Long userId = getCurrentUserId();
        validatePlainContent(request.content());
        activityOutbox.lockPost(id);
        Post post = findById(id);
        boolean wasDraft = Boolean.TRUE.equals(post.getIsDraft());
        post.preserveLegacyPublication();
        Set<Long> retainedIds =
                request.retainedMediaIds() == null
                        ? Set.of()
                        : Set.copyOf(request.retainedMediaIds());
        if (post.getImages().stream().filter(media -> retainedIds.contains(media.getId())).count()
                != retainedIds.size()) {
            throw new until.the.eternity.dcs.domain.post.exception.PostContractException(
                    until.the.eternity.dcs.domain.post.exception.PostContractExceptionCode
                            .INVALID_MEDIA);
        }
        List<MultipartFile> files = newFiles == null ? List.of() : newFiles;
        List<PostImage> retained =
                post.getImages().stream()
                        .filter(media -> retainedIds.contains(media.getId()))
                        .toList();
        if (request.retainedMediaIds() != null) {
            for (int i = 0; i < request.retainedMediaIds().size(); i++) {
                Long mediaId = request.retainedMediaIds().get(i);
                retained.stream()
                        .filter(media -> media.getId().equals(mediaId))
                        .findFirst()
                        .orElseThrow()
                        .changeDisplayOrder(i);
            }
        }
        if (retained.size() + files.size() > 5) {
            throw new until.the.eternity.dcs.domain.post.exception.PostContractException(
                    until.the.eternity.dcs.domain.post.exception.PostContractExceptionCode
                            .MEDIA_COUNT_EXCEEDED);
        }
        List<PostMediaValidator.ValidatedMedia> validated =
                files.stream().map(PostMediaValidator::validate).toList();
        long videoCount =
                retained.stream()
                                .filter(media -> media.getMediaType() == PostMediaType.VIDEO)
                                .count()
                        + validated.stream()
                                .filter(media -> media.type() == PostMediaType.VIDEO)
                                .count();
        if (videoCount > 1) {
            throw new until.the.eternity.dcs.domain.post.exception.PostContractException(
                    until.the.eternity.dcs.domain.post.exception.PostContractExceptionCode
                            .MEDIA_COUNT_EXCEEDED);
        }

        List<String> uploaded = new ArrayList<>();
        try {
            List<PostImage> removed =
                    post.getImages().stream()
                            .filter(media -> !retainedIds.contains(media.getId()))
                            .toList();
            removed.forEach(post::removeMedia);
            for (int i = 0; i < files.size(); i++) {
                MultipartFile file = files.get(i);
                String stored = minioService.uploadFile(file);
                uploaded.add(stored);
                PostMediaValidator.ValidatedMedia media = validated.get(i);
                post.addMedia(
                        file.getOriginalFilename(),
                        stored,
                        media.type(),
                        media.mimeType(),
                        file.getSize(),
                        retained.size() + i);
            }
            post.updatePlainContent(request.content(), userId);
            Post saved = postRepository.save(post);
            if (wasDraft && post.markFirstPublication())
                activityOutbox.created(ActivitySource.POST, id, post.getUserId(), id);
            registerMediaCleanup(removed, List.copyOf(uploaded));
            return postConverter.fromPostToPostPersistResponse(saved);
        } catch (Exception e) {
            uploaded.forEach(
                    name -> {
                        try {
                            minioService.deleteFile(name);
                        } catch (Exception cleanupFailure) {
                            log.error("수정 롤백 중 미디어 삭제 실패: {}", name, cleanupFailure);
                        }
                    });
            throw e;
        }
    }

    private void registerMediaCleanup(List<PostImage> removed, List<String> uploaded) {
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        removed.forEach(media -> safeDelete(media.getStoredFileName()));
                    }

                    @Override
                    public void afterCompletion(int status) {
                        if (status != TransactionSynchronization.STATUS_COMMITTED) {
                            uploaded.forEach(PostService.this::safeDelete);
                        }
                    }
                });
    }

    private void registerUploadedMediaRollback(List<String> uploaded) {
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCompletion(int status) {
                        if (status != TransactionSynchronization.STATUS_COMMITTED) {
                            uploaded.forEach(PostService.this::safeDelete);
                        }
                    }
                });
    }

    private void safeDelete(String storedName) {
        try {
            minioService.deleteFile(storedName);
        } catch (Exception e) {
            log.error("미디어 정리 실패: {}", storedName, e);
        }
    }

    @Transactional
    @PreAuthorize("@postPermissionEvaluator.canDelete(authentication,#id)")
    public void deletePost(Long id) {
        Long userId = getCurrentUserId();
        activityOutbox.lockPost(id);
        Post post = findById(id);
        activityOutbox.deletedRoot("POST", id);
        post.delete(userId);
        post.getComments().forEach(Comment::disconnectWithPost);
    }

    @Transactional
    @PreAuthorize("@postPermissionEvaluator.canTogglePostLike(authentication)")
    public void togglePostLike(PostLikeCreateRequest postLikeCreateRequest) {
        Long userId = getCurrentUserId();
        Long postId = postLikeCreateRequest.postId();
        activityOutbox.lockPost(postId);
        Post post = findById(postId);
        String userIp =
                checkIsAnonymousUser() ? getCurrentUserIp() : String.valueOf(getCurrentUserId());

        if (!postLikeRepository.existsByUserIdAndPostId(userId, postId)) {
            if (!postMetaService.canDoMethod(postId, PostMetaType.LIKE.getCode(), userIp)) {
                return;
            }
            PostLike newPostLike = postLikeConverter.toEntity(userId, post);

            postLikeRepository.save(newPostLike);
            if (!Boolean.TRUE.equals(post.getIsDraft()))
                activityOutbox.changed(
                        ActivitySource.POST_LIKE, postId, userId, post.getUserId(), postId, true);
            postMetaService.likePost(postId, userIp);

            redisSender.enqueue(NotificationJob.of(post.getUserId(), POST_LIKE, postId));
            return;
        }
        if (!postMetaService.canDoMethod(postId, PostMetaType.UNLIKE.getCode(), userIp)) {
            return;
        }
        postLikeRepository.deleteByUserIdAndPostId(userId, postId);
        activityOutbox.changed(
                ActivitySource.POST_LIKE, postId, userId, post.getUserId(), postId, false);
        postMetaService.unlikePost(postId, userIp);
    }

    public Page<PostSummaryResponse> findPostsByBoardId(PostPageRequest request, Long boardId) {
        Pageable pageable = request.toPageable();
        String[] sortArr = pageable.getSort().toString().split(":");
        Page<Post> posts;
        if (sortArr[0].equals("likeCount") || sortArr[0].equals("viewCount")) {
            Pageable newPageable = adjustSort(pageable);

            Board board = boardService.findBoardById(boardId);

            posts = postRepository.findWithPostMetaByBoardId(newPageable, board);
        } else {
            posts =
                    postRepository.findAllByBoardIdAndIsDeletedFalseAndIsBlockedFalse(
                            pageable, boardId);
        }

        return getPostSummaryResponses(posts);
    }

    public Page<PostSummaryResponse> searchPosts(PostPageRequest request, String keyword) {
        Page<Post> posts = postRepository.findWithPostMetaByKeyword(request.toPageable(), keyword);

        return getPostSummaryResponses(posts);
    }

    public Page<PostSummaryResponse> searchPostsByBoardId(
            PostPageRequest request, Long boardId, String keyword) {
        Board board = boardService.findBoardById(boardId);
        Page<Post> posts =
                postRepository.findWithPostMetaByBoardIdAndKeyword(
                        request.toPageable(), board, keyword);

        return getPostSummaryResponses(posts);
    }

    private Page<PostSummaryResponse> getPostSummaryResponses(Page<Post> posts) {
        List<Long> postIds = posts.getContent().stream().map(Post::getId).toList();
        List<Long> userIds = posts.getContent().stream().map(Post::getUserId).distinct().toList();
        Map<Long, PostMetaResponse> postMetaMap = postMetaService.getPostMetaInfos(postIds);
        Map<Long, UserSummary> userSummaryMap =
                userSummaryService.findByIdIn(userIds).stream()
                        .collect(Collectors.toMap(UserSummary::getId, u -> u));

        return posts.map(
                post ->
                        PostSummaryResponse.of(
                                post,
                                postMetaMap.get(post.getId()),
                                userSummaryMap.get(post.getUserId())));
    }

    public Page<PostSummaryResponse> searchPostsByUserId(PostPageRequest request, Long userId) {
        Page<Post> posts = postRepository.findWithPostMetaByUserId(request.toPageable(), userId);

        return getPostSummaryResponses(posts);
    }

    public Page<PostSummaryResponse> getPopularPostsByBoardId(
            PostPageRequest request, Long boardId) {
        Board board = boardService.findBoardById(boardId);
        Page<Post> posts = postRepository.findPopularPostsByBoardId(request.toPageable(), board);
        return getPostSummaryResponses(posts);
    }

    public Page<PostSummaryResponse> getMostLikedPostsByBoardId(
            PostPageRequest request, Long boardId) {
        Board board = boardService.findBoardById(boardId);
        Page<Post> posts = postRepository.findMostLikedPostsByBoardId(request.toPageable(), board);
        return getPostSummaryResponses(posts);
    }

    public Page<PostSummaryResponse> getPopularPosts(PostPageRequest request) {
        Page<Post> posts = postRepository.findPopularPosts(request.toPageable());
        return getPostSummaryResponses(posts);
    }

    private Post findById(Long id) {
        return postRepository.findWithTagsById(id).orElseThrow(() -> new PostNotFoundException(id));
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return postPermissionEvaluator.getCurrentUserId(auth);
    }

    private String getCurrentUserIp() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        Object details = auth.getDetails();
        if (details instanceof CustomWebAuthenticationDetails webDetails) {
            return webDetails.getRealRemoteAddress();
        }

        return "UNKNOWN_IP";
    }

    private boolean checkIsAnonymousUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return postPermissionEvaluator.isAnonymousUser(auth);
    }

    private Long getOptionalCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof Long id)) {
            return null;
        }
        return id;
    }

    private void validatePlainContent(String content) {
        if (content == null
                || content.isBlank()
                || content.codePointCount(0, content.length()) > 1000) {
            throw new until.the.eternity.dcs.domain.post.exception.PostContractException(
                    until.the.eternity.dcs.domain.post.exception.PostContractExceptionCode
                            .INVALID_PLAIN_CONTENT);
        }
    }

    private void validateMediaSet(List<MultipartFile> files) {
        if (files.size() > 5) {
            throw new until.the.eternity.dcs.domain.post.exception.PostContractException(
                    until.the.eternity.dcs.domain.post.exception.PostContractExceptionCode
                            .MEDIA_COUNT_EXCEEDED);
        }
        long videos =
                files.stream()
                        .map(PostMediaValidator::validate)
                        .filter(media -> media.type() == PostMediaType.VIDEO)
                        .count();
        if (videos > 1) {
            throw new until.the.eternity.dcs.domain.post.exception.PostContractException(
                    until.the.eternity.dcs.domain.post.exception.PostContractExceptionCode
                            .MEDIA_COUNT_EXCEEDED);
        }
    }

    private Page<PostFeedItem> toFeedItems(Page<Post> posts, Long currentUserId) {
        List<Long> postIds = posts.stream().map(Post::getId).toList();
        List<Long> userIds = posts.stream().map(Post::getUserId).distinct().toList();
        Map<Long, PostMetaResponse> meta = postMetaService.getPostMetaInfos(postIds);
        Map<Long, UserSummary> users =
                userSummaryService.findByIdIn(userIds).stream()
                        .collect(Collectors.toMap(UserSummary::getId, user -> user));
        Set<Long> liked =
                currentUserId == null
                        ? Set.of()
                        : Set.copyOf(
                                postLikeRepository.findPostIdsByUserIdAndPostIdIn(
                                        currentUserId, postIds));
        Set<Long> bookmarked =
                currentUserId == null
                        ? Set.of()
                        : Set.copyOf(
                                postBookmarkRepository.findPostIdsByUserIdAndPostIdIn(
                                        currentUserId, postIds));
        return posts.map(
                post -> {
                    UserSummary user = users.get(post.getUserId());
                    PostMetaResponse counts = meta.get(post.getId());
                    List<PostFeedItem.Media> media =
                            post.getImages().stream()
                                    .sorted(
                                            java.util.Comparator.comparingInt(
                                                    PostImage::getDisplayOrder))
                                    .map(
                                            item ->
                                                    new PostFeedItem.Media(
                                                            item.getId(),
                                                            item.getMediaType().name(),
                                                            minioService.getFileUrl(
                                                                    item.getStoredFileName()),
                                                            item.getMimeType(),
                                                            item.getDisplayOrder()))
                                    .toList();
                    PostFeedItem.Author author =
                            new PostFeedItem.Author(
                                    user == null ? "알수없음" : user.getNickname(),
                                    user == null ? null : user.getProfileImageUrl(),
                                    user == null ? null : user.getServerName(),
                                    user != null && user.isVerified(),
                                    user == null ? null : user.getActivityGrade());
                    return new PostFeedItem(
                            post.getId(),
                            post.getUserId(),
                            author,
                            post.readableContent(),
                            media,
                            counts == null ? 0 : counts.viewCount(),
                            counts == null ? 0 : counts.likeCount(),
                            counts == null ? 0 : counts.commentCount(),
                            liked.contains(post.getId()),
                            bookmarked.contains(post.getId()),
                            post.getCreatedAt(),
                            post.getUpdatedAt());
                });
    }

    private Pageable adjustSort(Pageable pageable) {

        Sort originalSort = pageable.getSort();

        List<Sort.Order> newOrders = new ArrayList<>();

        for (Sort.Order order : originalSort) {
            String property = order.getProperty();
            Sort.Order newOrder;

            if ("likeCount".equals(property)) {
                newOrder = new Sort.Order(order.getDirection(), "likeCount");
            } else if ("viewCount".equals(property)) {
                newOrder = new Sort.Order(order.getDirection(), "viewCount");
            } else {
                newOrder = new Sort.Order(order.getDirection(), "p." + property);
            }
            newOrders.add(newOrder);
        }

        Sort newSort = Sort.by(newOrders);

        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), newSort);
    }
}
