package until.the.eternity.dcs.domain.post.application;

import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import until.the.eternity.dcs.domain.post.exception.PostDeletionNotAllowedException;
import until.the.eternity.dcs.domain.post.exception.PostModifyForbiddenException;
import until.the.eternity.dcs.domain.post.exception.PostNotFoundException;
import until.the.eternity.dcs.domain.post.infrastructure.PostLikeRepository;
import until.the.eternity.dcs.domain.post.infrastructure.PostMetaRepository;
import until.the.eternity.dcs.domain.post.infrastructure.PostRepository;
import until.the.eternity.dcs.domain.tag.application.PostTagService;
import until.the.eternity.dcs.domain.tag.application.TagService;
import until.the.eternity.dcs.domain.tag.entity.PostTag;
import until.the.eternity.dcs.domain.user.application.UserService;
import until.the.eternity.dcs.domain.user.entity.UserSummary;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final PostConverter postConverter;
    private final UserService fakeUserService;
    private final PostLikeRepository postLikeRepository;
    private final PostLikeConverter postLikeConverter;
    private final PostMetaRepository postMetaRepository;
    private final TagService tagService;
    private final PostTagService postTagService;

    // todo 추후에 사용자 인증부분 추가해야될듯(token 유효라던가)
    // todo postTag 관련 로직도 추후 필요
    @Transactional
    public PostPersistResponse createPost(PostCreateRequest request) {
        UserSummary user = getCurrentUser();

        Post post = postConverter.fromCreateRequestToPost(request, user.getId());
        Post savedPost = postRepository.save(post);

        List<PostTag> postTags =
                request.tags().stream()
                        .map(tagService::findOrCreateTag) // 태그를 찾거나 새로 생성
                        .map(
                                tag ->
                                        PostTag.builder()
                                                .post(savedPost)
                                                .tag(tag)
                                                .build()) // PostTag 엔티티 생성
                        .toList();

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
    public PostPersistResponse updatePost(Long id, PostUpdateRequest postUpdateRequest) {

        UserSummary user = getCurrentUser();

        // 우선 본인 글만 수정할 수 있게 (ADMIN 권한도 게시글 내용을 수정할 수 있게 할건지)
        if (!Objects.equals(user.getId(), postUpdateRequest.userId())) {
            throw new PostModifyForbiddenException();
        }

        Post post = findById(id);

        List<String> newTags = postUpdateRequest.tags();

        List<PostTag> newPostTags =
                newTags.stream()
                        .map(tagService::findOrCreateTag) // 태그를 찾거나 새로 생성
                        .map(tag -> PostTag.builder().post(post).tag(tag).build()) // PostTag 엔티티 생성
                        .toList();

        post.getPostTags().clear();

        post.update(
                postUpdateRequest.title(),
                postUpdateRequest.content(),
                postUpdateRequest.isDraft(),
                newPostTags,
                user.getId());

        return postConverter.fromPostToPostPersistResponse(postRepository.save(post));
    }

    @Transactional
    public void deletePost(Long id) {
        UserSummary user = getCurrentUser();
        Post post = findById(id);

        if (!Objects.equals(user.getId(), post.getUserId())) {
            throw new PostDeletionNotAllowedException();
        }

        postRepository.delete(post);
    }

    @Transactional
    public void togglePostLike(PostLikeCreateRequest postLikeCreateRequest) {
        Long userId = getCurrentUser().getId();
        Long postId = postLikeCreateRequest.postId();
        Post post = findById(postId);
        PostMeta postMeta = findPostMetaByPostId(postId);

        if (!postLikeRepository.existsByUserIdAndPostId(userId, postId)) {
            PostLike newPostLike = postLikeConverter.toEntity(userId, post);

            postLikeRepository.save(newPostLike);
            postMeta.like();
            postMetaRepository.save(postMeta);
            return;
        }
        postLikeRepository.deleteByUserIdAndPostId(userId, postId);
        postMeta.unlike();
        postMetaRepository.save(postMeta);
    }

    private UserSummary getCurrentUser() {
        return fakeUserService.getCurrentUser();
    }

    private Post findById(Long id) {
        return postRepository.findWithTagsById(id).orElseThrow(() -> new PostNotFoundException(id));
    }

    private PostMeta findPostMetaByPostId(Long postId) {
        return postMetaRepository.findByPostId(postId).orElse(PostMeta.create(postId));
    }
}
