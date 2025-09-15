package until.the.eternity.dcs.domain.post.application;

import static until.the.eternity.dcs.domain.notice.enums.NoticeType.POST_LIKE;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
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
import until.the.eternity.dcs.domain.post.dto.request.PostCreateRequest;
import until.the.eternity.dcs.domain.post.dto.request.PostLikeCreateRequest;
import until.the.eternity.dcs.domain.post.dto.request.PostUpdateRequest;
import until.the.eternity.dcs.domain.post.dto.response.PostDetailResponse;
import until.the.eternity.dcs.domain.post.dto.response.PostPersistResponse;
import until.the.eternity.dcs.domain.post.dto.response.PostSummaryResponse;
import until.the.eternity.dcs.domain.post.entity.Post;
import until.the.eternity.dcs.domain.post.entity.PostLike;
import until.the.eternity.dcs.domain.post.entity.PostMeta;
import until.the.eternity.dcs.domain.post.exception.PostNotFoundException;
import until.the.eternity.dcs.domain.post.infrastructure.PostLikeRepository;
import until.the.eternity.dcs.domain.post.infrastructure.PostMetaRepository;
import until.the.eternity.dcs.domain.post.infrastructure.PostRepository;
import until.the.eternity.dcs.domain.tag.application.PostTagService;
import until.the.eternity.dcs.domain.tag.application.TagService;
import until.the.eternity.dcs.domain.tag.entity.PostTag;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final PostConverter postConverter;
    private final PostLikeRepository postLikeRepository;
    private final PostLikeConverter postLikeConverter;
    private final PostMetaRepository postMetaRepository;
    private final TagService tagService;
    private final PostTagService postTagService;
    private final RedisSender redisSender;
    private final PostPermissionEvaluator postPermissionEvaluator;

    @Transactional
    @PreAuthorize("@postPermissionEvaluator.canCreate(authentication)")
    public PostPersistResponse createPost(PostCreateRequest request) {

        Long userId = getCurrentUserId();
        Post post = postConverter.fromCreateRequestToPost(request, userId);
        Post savedPost = postRepository.save(post);

        List<PostTag> postTags =
                request.tags().stream()
                        .map(tagService::findOrCreateTag)
                        .map(tag -> PostTag.builder().post(savedPost).tag(tag).build())
                        .collect(Collectors.toList());

        postTagService.savePostTags(postTags);

        return postConverter.fromPostToPostPersistResponse(savedPost);
    }

    public PostDetailResponse findPost(Long id) {
        Post post = findById(id);
        PostMeta postMeta = findPostMetaByPostId(id);
        postMeta.viewPost(); // todo 차후 일정 기간 내 다시 조회는 조회수 카운트로 치지 않도록 변경
        postMetaRepository.save(postMeta);
        return postConverter.fromPostToPostDetailResponse(post, postMeta);
    }

    public Page<PostSummaryResponse> findPosts(CustomPageRequest request) {
        Pageable pageable = request.toPageable();

        Page<Post> posts = postRepository.findAllByIsDeletedFalseAndIsBlockedFalse(pageable);
        Map<Long, PostMeta> PostMetaMap = new HashMap<>();
        for (Post post : posts) {
            PostMeta postMeta = findPostMetaByPostId(post.getId());
            PostMetaMap.put(post.getId(), postMeta);
        }
        return posts.map(post -> PostSummaryResponse.from(post, PostMetaMap.get(post.getId())));
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
    }

    @Transactional
    @PreAuthorize("@postPermissionEvaluator.canTogglePostLike(authentication)")
    public void togglePostLike(PostLikeCreateRequest postLikeCreateRequest) {
        Long userId = getCurrentUserId();
        Long postId = postLikeCreateRequest.postId();
        Post post = findById(postId);
        PostMeta postMeta = findPostMetaByPostId(postId);

        if (!postLikeRepository.existsByUserIdAndPostId(userId, postId)) {
            PostLike newPostLike = postLikeConverter.toEntity(userId, post);

            postLikeRepository.save(newPostLike);
            postMeta.like();
            postMetaRepository.save(postMeta);

            redisSender.enqueue(NotificationJob.of(post.getUserId(), POST_LIKE, postId));
            return;
        }
        postLikeRepository.deleteByUserIdAndPostId(userId, postId);
        postMeta.unlike();
        postMetaRepository.save(postMeta);
    }

    public Page<PostSummaryResponse> findPostsByBoardId(CustomPageRequest request, Long boardId) {
        Pageable pageable = request.toPageable();

        Page<Post> posts =
                postRepository.findAllByBoardIdAndIsDeletedFalseAndIsBlockedFalse(
                        pageable, boardId);
        Map<Long, PostMeta> PostMetaMap = new HashMap<>();
        for (Post post : posts) {
            PostMeta postMeta = findPostMetaByPostId(post.getId());
            PostMetaMap.put(post.getId(), postMeta);
        }
        return posts.map(post -> PostSummaryResponse.from(post, PostMetaMap.get(post.getId())));
    }

    private Post findById(Long id) {
        return postRepository.findWithTagsById(id).orElseThrow(() -> new PostNotFoundException(id));
    }

    private PostMeta findPostMetaByPostId(Long postId) {
        return postMetaRepository.findByPostId(postId).orElse(PostMeta.create(postId));
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return postPermissionEvaluator.getCurrentUserId(auth);
    }
}
