package until.the.eternity.dcs.domain.post.application;

import static until.the.eternity.dcs.domain.notice.enums.NoticeType.POST_LIKE;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
import org.springframework.web.multipart.MultipartFile;
import until.the.eternity.dcs.common.entity.CustomWebAuthenticationDetails;
import until.the.eternity.dcs.common.infrastructure.MinioService;
import until.the.eternity.dcs.common.notification.RedisSender;
import until.the.eternity.dcs.common.notification.dto.NotificationJob;
import until.the.eternity.dcs.common.request.CustomPageRequest;
import until.the.eternity.dcs.domain.board.application.BoardService;
import until.the.eternity.dcs.domain.board.entity.Board;
import until.the.eternity.dcs.domain.comment.entity.Comment;
import until.the.eternity.dcs.domain.post.dto.request.PostCreateRequest;
import until.the.eternity.dcs.domain.post.dto.request.PostLikeCreateRequest;
import until.the.eternity.dcs.domain.post.dto.request.PostUpdateRequest;
import until.the.eternity.dcs.domain.post.dto.response.PostDetailResponse;
import until.the.eternity.dcs.domain.post.dto.response.PostMetaResponse;
import until.the.eternity.dcs.domain.post.dto.response.PostPersistResponse;
import until.the.eternity.dcs.domain.post.dto.response.PostSummaryResponse;
import until.the.eternity.dcs.domain.post.entity.Post;
import until.the.eternity.dcs.domain.post.entity.PostLike;
import until.the.eternity.dcs.domain.post.enums.PostMetaType;
import until.the.eternity.dcs.domain.post.exception.PostNotFoundException;
import until.the.eternity.dcs.domain.post.infrastructure.PostLikeRepository;
import until.the.eternity.dcs.domain.post.infrastructure.PostRepository;
import until.the.eternity.dcs.domain.tag.application.PostTagService;
import until.the.eternity.dcs.domain.tag.application.TagService;
import until.the.eternity.dcs.domain.tag.entity.PostTag;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
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

    @Transactional
    @PreAuthorize("@postPermissionEvaluator.canCreate(authentication)")
    public PostPersistResponse createPost(PostCreateRequest request, List<MultipartFile> files) {

        List<String> uploadedFileNames = new ArrayList<>();

        Long userId = getCurrentUserId();
        Post post = postConverter.fromCreateRequestToPost(request, userId);
        Post savedPost = postRepository.save(post);

        List<PostTag> postTags =
                request.tags().stream()
                        .map(tagService::findOrCreateTag)
                        .map(tag -> PostTag.builder().post(savedPost).tag(tag).build())
                        .collect(Collectors.toList());
        if (files != null && !files.isEmpty()) {
            if (files.size() > 5) {
                throw new IllegalArgumentException("이미지는 최대 5개까지 업로드 가능합니다.");
            }
            try {
                for (MultipartFile file : files) {
                    String storedFileName = minioService.uploadFile(file);
                    uploadedFileNames.add(storedFileName);

                    savedPost.addImage(file.getOriginalFilename(), storedFileName);
                }
            } catch (Exception e) {
                for (String fileName : uploadedFileNames) {
                    try {
                        minioService.deleteFile(fileName);
                    } catch (Exception ignore) {
                        log.error("롤백 중 파일 삭제 실패: {}", fileName);
                    }
                }
                throw e;
            }
        }

        postTagService.savePostTags(postTags);
        postMetaService.createPostMeta(savedPost.getId());
        return postConverter.fromPostToPostPersistResponse(savedPost);
    }

    public PostDetailResponse findPost(Long id) {
        log.info("게시글 단건조회 테스트");
        Post post = findById(id);
        log.info("게시글 단건조회: {}", post.toString());
        String userIp =
                checkIsAnonymousUser() ? getCurrentUserIp() : String.valueOf(getCurrentUserId());
        log.info("userIp 값 테스트: {}", userIp);
        List<String> imageUrls =
                post.getImages().stream()
                        .map(image -> minioService.getFileUrl(image.getStoredFileName()))
                        .collect(Collectors.toList());

        postMetaService.viewPost(id, userIp);
        PostMetaResponse postMeta = postMetaService.getPostMetaInfo(id);
        return postConverter.fromPostToPostDetailResponse(post, postMeta, imageUrls);
    }

    public Page<PostSummaryResponse> findPosts(CustomPageRequest request) {
        Pageable pageable = request.toPageable();

        Page<Post> posts = postRepository.findAllByIsDeletedFalseAndIsBlockedFalse(pageable);
        List<Long> postIds = posts.getContent().stream().map(Post::getId).toList();

        Map<Long, PostMetaResponse> postMetaMap = postMetaService.getPostMetaInfos(postIds);
        return posts.map(post -> PostSummaryResponse.of(post, postMetaMap.get(post.getId())));
    }

    @Transactional
    @PreAuthorize("@postPermissionEvaluator.canUpdate(authentication,#id)")
    public PostPersistResponse updatePost(Long id, PostUpdateRequest postUpdateRequest) {
        Long userId = getCurrentUserId();

        Post post = findById(id);

        List<String> currentList =
                post.getPostTags().stream().map(postTag -> postTag.getTag().getName()).toList();

        List<String> newTags = postUpdateRequest.tags();

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

        post.update(
                postUpdateRequest.title(),
                postUpdateRequest.content(),
                postUpdateRequest.isDraft(),
                null,
                userId);

        Post updatedPost = postRepository.save(post);

        return postConverter.fromPostToPostPersistResponse(updatedPost);
    }

    @Transactional
    @PreAuthorize("@postPermissionEvaluator.canDelete(authentication,#id)")
    public void deletePost(Long id) {
        Long userId = getCurrentUserId();
        Post post = findById(id);
        post.delete(userId);
        post.getComments().forEach(Comment::disconnectWithPost);
    }

    @Transactional
    @PreAuthorize("@postPermissionEvaluator.canTogglePostLike(authentication)")
    public void togglePostLike(PostLikeCreateRequest postLikeCreateRequest) {
        Long userId = getCurrentUserId();
        Long postId = postLikeCreateRequest.postId();
        Post post = findById(postId);
        String userIp =
                checkIsAnonymousUser() ? getCurrentUserIp() : String.valueOf(getCurrentUserId());

        if (!postLikeRepository.existsByUserIdAndPostId(userId, postId)) {
            if (!postMetaService.canDoMethod(postId, PostMetaType.LIKE.getCode(), userIp)) {
                return;
            }
            PostLike newPostLike = postLikeConverter.toEntity(userId, post);

            postLikeRepository.save(newPostLike);
            postMetaService.likePost(postId, userIp);

            redisSender.enqueue(NotificationJob.of(post.getUserId(), POST_LIKE, postId));
            return;
        }
        if (!postMetaService.canDoMethod(postId, PostMetaType.UNLIKE.getCode(), userIp)) {
            return;
        }
        postLikeRepository.deleteByUserIdAndPostId(userId, postId);
        postMetaService.unlikePost(postId, userIp);
    }

    public Page<PostSummaryResponse> findPostsByBoardId(CustomPageRequest request, Long boardId) {
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
        log.info("게시판별 게시글 조회 로깅 테스트");
        List<Long> postIds = posts.getContent().stream().map(Post::getId).toList();
        log.info("게시글 id 리스트: {}", postIds.toString());
        Map<Long, PostMetaResponse> postMetaMap = postMetaService.getPostMetaInfos(postIds);
        return posts.map(post -> PostSummaryResponse.of(post, postMetaMap.get(post.getId())));
    }

    public Page<PostSummaryResponse> searchPosts(CustomPageRequest request, String keyword) {
        Page<Post> posts = postRepository.findWithPostMetaByKeyword(request.toPageable(), keyword);

        List<Long> postIds = posts.getContent().stream().map(Post::getId).toList();
        Map<Long, PostMetaResponse> postMetaMap = postMetaService.getPostMetaInfos(postIds);
        return posts.map(post -> PostSummaryResponse.of(post, postMetaMap.get(post.getId())));
    }

    public Page<PostSummaryResponse> searchPostsByBoardId(
            CustomPageRequest request, Long boardId, String keyword) {
        Board board = boardService.findBoardById(boardId);
        Page<Post> posts =
                postRepository.findWithPostMetaByBoardIdAndKeyword(
                        request.toPageable(), board, keyword);

        List<Long> postIds = posts.getContent().stream().map(Post::getId).toList();
        Map<Long, PostMetaResponse> postMetaMap = postMetaService.getPostMetaInfos(postIds);
        return posts.map(post -> PostSummaryResponse.of(post, postMetaMap.get(post.getId())));
    }

    public Page<PostSummaryResponse> searchPostsByUserId(CustomPageRequest request, Long userId) {
        Page<Post> posts = postRepository.findWithPostMetaByUserId(request.toPageable(), userId);

        List<Long> postIds = posts.getContent().stream().map(Post::getId).toList();
        Map<Long, PostMetaResponse> postMetaMap = postMetaService.getPostMetaInfos(postIds);
        return posts.map(post -> PostSummaryResponse.of(post, postMetaMap.get(post.getId())));
    }

    public Page<PostSummaryResponse> getPopularPostsByBoardId(
            CustomPageRequest request, Long boardId) {
        Board board = boardService.findBoardById(boardId);
        Page<Post> posts = postRepository.findPopularPostsByBoardId(request.toPageable(), board);
        List<Long> postIds = posts.getContent().stream().map(Post::getId).toList();
        Map<Long, PostMetaResponse> postMetaMap = postMetaService.getPostMetaInfos(postIds);
        return posts.map(post -> PostSummaryResponse.of(post, postMetaMap.get(post.getId())));
    }

    public Page<PostSummaryResponse> getMostLikedPostsByBoardId(
            CustomPageRequest request, Long boardId) {
        Board board = boardService.findBoardById(boardId);
        Page<Post> posts = postRepository.findMostLikedPostsByBoardId(request.toPageable(), board);
        List<Long> postIds = posts.getContent().stream().map(Post::getId).toList();
        Map<Long, PostMetaResponse> postMetaMap = postMetaService.getPostMetaInfos(postIds);
        return posts.map(post -> PostSummaryResponse.of(post, postMetaMap.get(post.getId())));
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

        Pageable newPageable =
                PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), newSort);
        return newPageable;
    }
}
