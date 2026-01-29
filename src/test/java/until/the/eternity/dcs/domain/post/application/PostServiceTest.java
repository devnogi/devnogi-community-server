package until.the.eternity.dcs.domain.post.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;
import until.the.eternity.dcs.common.notification.RedisSender;
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
import until.the.eternity.dcs.domain.post.entity.PostMeta;
import until.the.eternity.dcs.domain.post.exception.PostNotFoundException;
import until.the.eternity.dcs.domain.post.infrastructure.PostLikeRepository;
import until.the.eternity.dcs.domain.post.infrastructure.PostRepository;
import until.the.eternity.dcs.domain.tag.application.PostTagService;
import until.the.eternity.dcs.domain.tag.application.TagService;
import until.the.eternity.dcs.domain.user.entity.UserSummary;

@ExtendWith(MockitoExtension.class)
@DisplayName("PostService 단위 테스트")
class PostServiceTest {

    @Mock private PostRepository postRepository;

    @Mock private PostConverter postConverter;

    @Mock private PostLikeRepository postLikeRepository;

    @Mock private PostLikeConverter postLikeConverter;

    @Mock private TagService tagService;

    @Mock private PostTagService postTagService;

    @Mock private RedisSender redisSender;

    @Mock private PostPermissionEvaluator postPermissionEvaluator;
    @Mock private SecurityContext securityContext;
    @Mock private Authentication authentication;
    @Mock private PostMetaService postMetaService;
    @Mock private BoardService boardService;
    @InjectMocks private PostService postService;

    private PostMeta postMeta;
    private PostMeta postMeta2;
    private PostMeta postMeta3;
    private UserSummary mockUser;
    private Post mockPost;
    private Post mockPost2;
    private Post mockPost3;
    private PostCreateRequest createRequest;
    private PostUpdateRequest updateRequest;
    private PostSummaryResponse mockSummaryResponse;
    private PostDetailResponse mockDetailResponse;
    private PostPersistResponse mockPersistResponse;
    List<Long> postIdList;
    Long userId = 1L;
    Board mockBoard;
    Map<Long, PostMetaResponse> dbMetaMap;
    PostMetaResponse postMetaResponse;
    PostMetaResponse postMetaResponse2;
    PostMetaResponse postMetaResponse3;

    @BeforeEach
    void setUp() {
        mockUser = UserSummary.builder().id(1L).nickname("testUser").build();

        mockBoard = Board.builder().id(1L).build();

        mockPost =
                Post.builder()
                        .id(1L)
                        .board(mockBoard)
                        .title("Test Title")
                        .content("Test Content")
                        .userId(1L)
                        .isDraft(false)
                        .isBlocked(false)
                        .comments(new ArrayList<>())
                        .postTags(new ArrayList<>())
                        .build();

        mockPost.setIsDeleted(false);

        mockPost2 =
                Post.builder()
                        .id(2L)
                        .board(mockBoard)
                        .title("Test Title2")
                        .content("Test Content2")
                        .userId(1L)
                        .isDraft(false)
                        .isBlocked(false)
                        .comments(new ArrayList<>())
                        .postTags(new ArrayList<>())
                        .build();

        mockPost3 =
                Post.builder()
                        .id(3L)
                        .board(mockBoard)
                        .title("Test Title3")
                        .content("Test Content3")
                        .userId(1L)
                        .isDraft(false)
                        .isBlocked(false)
                        .comments(new ArrayList<>())
                        .postTags(new ArrayList<>())
                        .build();

        mockPost2.setIsDeleted(false);

        createRequest =
                new PostCreateRequest(
                        1L, "New Post", "New Content", false, Arrays.asList("tag1", "tag2"));

        updateRequest =
                new PostUpdateRequest(
                        "Updated Title", "Updated Content", false, Arrays.asList("tag3", "tag4"));

        mockSummaryResponse =
                PostSummaryResponse.builder()
                        .id(1L)
                        .title("Test Title")
                        .viewCount(0)
                        .likeCount(0)
                        .build();

        mockDetailResponse =
                PostDetailResponse.builder()
                        .id(1L)
                        .title("Test Title")
                        .content("Test Content")
                        .viewCount(1)
                        .build();
        mockPersistResponse = PostPersistResponse.builder().id(1L).build();

        postMeta = PostMeta.builder().postId(1L).viewCount(0).likeCount(0).build();
        postMeta2 = PostMeta.builder().postId(2L).viewCount(0).likeCount(0).build();
        postMeta3 =
                PostMeta.builder().postId(3L).viewCount(22).likeCount(30).commentCount(10).build();
        postIdList = new ArrayList<>();
        dbMetaMap = new HashMap<>();

        postMetaResponse = PostMetaResponse.from(postMeta);
        postMetaResponse2 = PostMetaResponse.from(postMeta2);
        postMetaResponse3 = PostMetaResponse.from(postMeta3);
    }

    @Nested
    @DisplayName("게시글 생성 테스트")
    class CreatePostTest {

        @Test
        @DisplayName("정상적인 게시글 생성")
        void createPost_Success() {
            // Given
            SecurityContextHolder.setContext(securityContext);
            given(securityContext.getAuthentication()).willReturn(authentication);
            given(postPermissionEvaluator.getCurrentUserId(authentication)).willReturn(userId);
            given(postConverter.fromCreateRequestToPost(eq(createRequest), eq(1L)))
                    .willReturn(mockPost);
            given(postRepository.save(mockPost)).willReturn(mockPost);
            given(postConverter.fromPostToPostPersistResponse(mockPost))
                    .willReturn(mockPersistResponse);
            List<MultipartFile> files = new ArrayList<>();
            // When
            PostPersistResponse result = postService.createPost(createRequest, files);

            // Then
            assertThat(result).isEqualTo(mockPersistResponse);
            verify(postConverter).fromCreateRequestToPost(eq(createRequest), eq(1L));
            verify(postRepository).save(mockPost);
            verify(postConverter).fromPostToPostPersistResponse(mockPost);
        }
    }

    @Nested
    @DisplayName("게시글 조회 테스트")
    class FindPostTest {

        @Test
        @DisplayName("정상적인 게시글 단건 조회")
        void findPost_Success() {
            // Given
            Long postId = 1L;
            List<Comment> comments = new ArrayList<>();

            Post postWithComments =
                    Post.builder()
                            .id(postId)
                            .title("Test Title")
                            .content("Test Content")
                            .userId(1L)
                            .comments(comments)
                            .build();
            List<String> imageList = new ArrayList<>();
            given(postMetaService.getPostMetaInfo(1L)).willReturn(postMetaResponse);
            given(postRepository.findWithTagsById(postId))
                    .willReturn(Optional.of(postWithComments));
            given(
                            postConverter.fromPostToPostDetailResponse(
                                    postWithComments, postMetaResponse, imageList))
                    .willReturn(mockDetailResponse);
            int cnt = postMeta.getViewCount();
            // When
            PostDetailResponse result = postService.findPost(postId);

            // Then
            assertThat(result).isEqualTo(mockDetailResponse);
            assertThat(result.viewCount()).isEqualTo(cnt + 1);
            assertThat(result.viewCount()).isEqualTo(postMeta.getViewCount() + 1);
            verify(postRepository).findWithTagsById(postId);
            verify(postConverter)
                    .fromPostToPostDetailResponse(postWithComments, postMetaResponse, imageList);
        }

        @Test
        @DisplayName("존재하지 않는 게시글 조회 시 예외 발생")
        void findPost_NotFound() {
            // Given
            Long postId = 999L;
            given(postRepository.findWithTagsById(postId)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> postService.findPost(postId))
                    .isInstanceOf(PostNotFoundException.class);

            verify(postRepository).findWithTagsById(postId);
        }

        @Test
        @DisplayName("게시글 목록 조회 성공")
        void findPosts_Success() {
            // Given
            postIdList.add(1L);
            postIdList.add(2L);
            CustomPageRequest pageRequest = mock(CustomPageRequest.class);

            dbMetaMap.put(postIdList.get(0), postMetaResponse);
            dbMetaMap.put(postIdList.get(1), postMetaResponse2);
            Pageable pageable = PageRequest.of(1, 10);
            List<Post> posts = Arrays.asList(mockPost, mockPost2);
            Page<Post> postPage = new PageImpl<>(posts, pageable, 1);

            given(pageRequest.toPageable()).willReturn(pageable);
            given(postRepository.findAllByIsDeletedFalseAndIsBlockedFalse(pageable))
                    .willReturn(postPage);
            given(postMetaService.getPostMetaInfos(anyList())).willReturn(dbMetaMap);

            // When
            Page<PostSummaryResponse> result = postService.findPosts(pageRequest);

            // Then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent().get(0)).isEqualTo(mockSummaryResponse);
            verify(postRepository).findAllByIsDeletedFalseAndIsBlockedFalse(pageable);
        }

        @Test
        @DisplayName("게시판에 있는 게시글 목록 조회 성공")
        void findPostsByBoardId_Success() {
            // Given
            Long boardId = mockBoard.getId();
            CustomPageRequest pageRequest = new CustomPageRequest(1, 10, "id", "desc");
            Pageable pageable = pageRequest.toPageable();

            List<Post> posts = Arrays.asList(mockPost, mockPost2);
            Page<Post> postPage = new PageImpl<>(posts, pageable, posts.size());

            postIdList.add(1L);
            postIdList.add(2L);
            dbMetaMap.put(postIdList.get(0), postMetaResponse);
            dbMetaMap.put(postIdList.get(1), postMetaResponse2);
            given(
                            postRepository.findAllByBoardIdAndIsDeletedFalseAndIsBlockedFalse(
                                    pageable, boardId))
                    .willReturn(postPage);
            given(postMetaService.getPostMetaInfos(anyList())).willReturn(dbMetaMap);

            // When
            Page<PostSummaryResponse> result = postService.findPostsByBoardId(pageRequest, boardId);

            // Then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent().get(0)).isEqualTo(mockSummaryResponse);
            verify(postRepository)
                    .findAllByBoardIdAndIsDeletedFalseAndIsBlockedFalse(pageable, boardId);
        }

        @Test
        @DisplayName("게시판에 있는 게시글 목록 조회 성공")
        void findPostsByBoardIdWithNewPageable_Success() {
            // Given
            Long boardId = mockBoard.getId();
            CustomPageRequest pageRequest = new CustomPageRequest(1, 10, "likeCount", "desc");
            Pageable pageable = pageRequest.toPageable();

            List<Post> posts = Arrays.asList(mockPost, mockPost2);
            Page<Post> postPage = new PageImpl<>(posts, pageable, posts.size());
            postIdList.add(1L);
            postIdList.add(2L);
            dbMetaMap.put(postIdList.get(0), postMetaResponse);
            dbMetaMap.put(postIdList.get(1), postMetaResponse2);
            ;
            given(postRepository.findWithPostMetaByBoardId(any(Pageable.class), eq(mockBoard)))
                    .willReturn(postPage);
            given(boardService.findBoardById(boardId)).willReturn(mockBoard);
            given(postMetaService.getPostMetaInfos(anyList())).willReturn(dbMetaMap);

            // When
            Page<PostSummaryResponse> result = postService.findPostsByBoardId(pageRequest, boardId);

            // Then
            assertThat(result.getContent()).hasSize(2);

            verify(postRepository, times(1))
                    .findWithPostMetaByBoardId(any(Pageable.class), eq(mockBoard));

            verify(postRepository, never())
                    .findAllByBoardIdAndIsDeletedFalseAndIsBlockedFalse(
                            any(Pageable.class), anyLong());
        }
    }

    @Nested
    @DisplayName("게시글 수정 테스트")
    class UpdatePostTest {

        @Test
        @DisplayName("정상적인 게시글 수정")
        void updatePost_Success() {
            // Given
            Long postId = 1L;
            SecurityContextHolder.setContext(securityContext);
            given(securityContext.getAuthentication()).willReturn(authentication);
            given(postPermissionEvaluator.getCurrentUserId(authentication)).willReturn(userId);
            given(postRepository.findWithTagsById(postId)).willReturn(Optional.of(mockPost));
            given(postRepository.save(mockPost)).willReturn(mockPost);
            given(postConverter.fromPostToPostPersistResponse(mockPost))
                    .willReturn(mockPersistResponse);

            // When
            PostPersistResponse result = postService.updatePost(postId, updateRequest);

            // Then
            assertThat(result).isEqualTo(mockPersistResponse);
            verify(postRepository).findWithTagsById(postId);
            verify(postRepository).save(mockPost);
        }

        @Test
        @DisplayName("존재하지 않는 게시글 수정 시 예외 발생")
        void updatePost_PostNotFound() {
            // Given
            Long postId = 999L;
            SecurityContextHolder.setContext(securityContext);
            given(securityContext.getAuthentication()).willReturn(authentication);
            given(postPermissionEvaluator.getCurrentUserId(authentication)).willReturn(userId);
            given(postRepository.findWithTagsById(postId)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> postService.updatePost(postId, updateRequest))
                    .isInstanceOf(PostNotFoundException.class);

            verify(postRepository).findWithTagsById(postId);
            verify(postRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("게시글 삭제 테스트")
    class DeletePostTest {

        @Test
        @DisplayName("정상적인 게시글 삭제")
        void deletePost_Success() {
            // Given
            Long postId = 1L;
            given(postRepository.findWithTagsById(postId)).willReturn(Optional.of(mockPost));

            // When
            postService.deletePost(postId);

            // Then
            verify(postRepository).findWithTagsById(postId);
        }

        @Test
        @DisplayName("존재하지 않는 게시글 삭제 시 예외 발생")
        void deletePost_PostNotFound() {
            // Given
            Long postId = 999L;
            given(postRepository.findWithTagsById(postId)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> postService.deletePost(postId))
                    .isInstanceOf(PostNotFoundException.class);

            verify(postRepository).findWithTagsById(postId);
            verify(postRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("게시글 좋아요 테스트")
    class LikePostTest {
        @Test
        @DisplayName("게시글 좋아요")
        public void likePost_Test() {
            // given
            SecurityContextHolder.setContext(securityContext);
            given(securityContext.getAuthentication()).willReturn(authentication);
            given(postPermissionEvaluator.getCurrentUserId(authentication)).willReturn(userId);
            PostLikeCreateRequest postLikeCreateRequest =
                    new PostLikeCreateRequest(mockPost.getId());
            given(postLikeRepository.existsByUserIdAndPostId(mockUser.getId(), mockPost.getId()))
                    .willReturn(false);
            given(postRepository.findWithTagsById(1L)).willReturn(Optional.of(mockPost));
            //            given(postMetaService.getPostMeta(1L)).willReturn(postMeta);
            given(postMetaService.canDoMethod(1L, "like", "1")).willReturn(true);

            int cnt = postMeta.getLikeCount();
            // when
            postService.togglePostLike(postLikeCreateRequest);

            // then
            verify(postMetaService).likePost(1L, "1");
            verify(postLikeRepository).existsByUserIdAndPostId(mockUser.getId(), mockPost.getId());
        }

        @Test
        @DisplayName("게시글 좋아요 해제")
        public void unlikePost_Test() {
            // given
            SecurityContextHolder.setContext(securityContext);
            given(securityContext.getAuthentication()).willReturn(authentication);
            given(postPermissionEvaluator.getCurrentUserId(authentication)).willReturn(userId);
            PostLikeCreateRequest postLikeCreateRequest =
                    new PostLikeCreateRequest(mockPost.getId());

            given(postLikeRepository.existsByUserIdAndPostId(mockUser.getId(), mockPost.getId()))
                    .willReturn(true);
            given(postRepository.findWithTagsById(1L)).willReturn(Optional.of(mockPost));
            given(postMetaService.canDoMethod(1L, "unlike", "1")).willReturn(true);
            int cnt = postMeta.getLikeCount();
            // when
            postService.togglePostLike(postLikeCreateRequest);

            // then
            verify(postMetaService).unlikePost(1L, "1");
            verify(postRepository).findWithTagsById(1L);
            verify(postLikeRepository).existsByUserIdAndPostId(mockUser.getId(), mockPost.getId());
            verify(postLikeRepository).deleteByUserIdAndPostId(mockUser.getId(), mockPost.getId());
        }
    }

    @Test
    @DisplayName("")
    void searchPosts_Success() {
        // given
        CustomPageRequest pageRequest = new CustomPageRequest(1, 10, "createdAt", "desc");
        String keyword = "keyword";
        Pageable pageable = pageRequest.toPageable();
        List<Post> posts = Arrays.asList(mockPost, mockPost2);
        Page<Post> postPage = new PageImpl<>(posts, pageable, 1);
        postIdList.add(1L);
        postIdList.add(2L);
        dbMetaMap.put(postIdList.get(0), postMetaResponse);
        dbMetaMap.put(postIdList.get(1), postMetaResponse2);
        given(postRepository.findWithPostMetaByKeyword(pageable, keyword)).willReturn(postPage);
        given(postMetaService.getPostMetaInfos(anyList())).willReturn(dbMetaMap);

        // when
        Page<PostSummaryResponse> responses = postService.searchPosts(pageRequest, keyword);

        // then
        List<PostSummaryResponse> list = responses.stream().toList();
        assertThat(list.size()).isEqualTo(2);
        assertThat(list.get(0).id()).isEqualTo(1);
        assertThat(list.get(1).id()).isEqualTo(2);
        assertThat(list.get(0).title()).isEqualTo(mockPost.getTitle());
        assertThat(list.get(1).title()).isEqualTo(mockPost2.getTitle());
    }

    @Test
    @DisplayName("키워드를 이용한 검색")
    public void searchPostsByBoardId_Success() {
        // given
        CustomPageRequest pageRequest = new CustomPageRequest(1, 10, "createdAt", "desc");
        Long boardId = 1L;
        String keyword = "keyword";
        Pageable pageable = pageRequest.toPageable();
        List<Post> posts = Arrays.asList(mockPost, mockPost2);
        Page<Post> postPage = new PageImpl<>(posts, pageable, 1);
        postIdList.add(1L);
        postIdList.add(2L);
        dbMetaMap.put(postIdList.get(0), postMetaResponse);
        dbMetaMap.put(postIdList.get(1), postMetaResponse2);
        given(boardService.findBoardById(boardId)).willReturn(mockBoard);
        given(postRepository.findWithPostMetaByBoardIdAndKeyword(pageable, mockBoard, keyword))
                .willReturn(postPage);
        given(postMetaService.getPostMetaInfos(anyList())).willReturn(dbMetaMap);

        // when
        Page<PostSummaryResponse> responses =
                postService.searchPostsByBoardId(pageRequest, boardId, keyword);

        // then
        List<PostSummaryResponse> list = responses.stream().toList();
        assertThat(list.size()).isEqualTo(2);
        assertThat(list.get(0).id()).isEqualTo(1);
        assertThat(list.get(1).id()).isEqualTo(2);
        assertThat(list.get(0).title()).isEqualTo(mockPost.getTitle());
        assertThat(list.get(1).title()).isEqualTo(mockPost2.getTitle());
    }

    @Test
    @DisplayName("사용자별 게시글 검색")
    public void searchPostsByUserId_Success() {
        // given
        CustomPageRequest pageRequest = new CustomPageRequest(1, 10, "createdAt", "desc");
        Long userId = 1L;
        Pageable pageable = pageRequest.toPageable();
        List<Post> posts = Arrays.asList(mockPost, mockPost2);
        Page<Post> postPage = new PageImpl<>(posts, pageable, 1);
        postIdList.add(1L);
        postIdList.add(2L);
        dbMetaMap.put(postIdList.get(0), postMetaResponse);
        dbMetaMap.put(postIdList.get(1), postMetaResponse2);
        given(postRepository.findWithPostMetaByUserId(pageable, userId)).willReturn(postPage);
        given(postMetaService.getPostMetaInfos(anyList())).willReturn(dbMetaMap);

        // when
        Page<PostSummaryResponse> responses = postService.searchPostsByUserId(pageRequest, userId);

        // then
        List<PostSummaryResponse> list = responses.stream().toList();
        assertThat(list.size()).isEqualTo(2);
        assertThat(list.get(0).id()).isEqualTo(1);
        assertThat(list.get(1).id()).isEqualTo(2);
        assertThat(list.get(0).title()).isEqualTo(mockPost.getTitle());
        assertThat(list.get(1).title()).isEqualTo(mockPost2.getTitle());
    }

    @Test
    @DisplayName("게시판 별 인기글 조회")
    public void getPopularPostByBoardId() {
        // given
        CustomPageRequest pageRequest = new CustomPageRequest(1, 10, "createdAt", "desc");
        Pageable pageable = pageRequest.toPageable();
        List<Post> posts = Arrays.asList(mockPost, mockPost2, mockPost3);
        Page<Post> postPage = new PageImpl<>(posts, pageable, 1);
        postIdList.add(1L);
        postIdList.add(2L);
        postIdList.add(3L);
        dbMetaMap.put(postIdList.get(0), postMetaResponse);
        dbMetaMap.put(postIdList.get(1), postMetaResponse2);
        dbMetaMap.put(postIdList.get(2), postMetaResponse3);
        mockPost.setCreatedAt(LocalDateTime.now().withNano(0));
        mockPost2.setCreatedAt(LocalDateTime.now().withNano(0));
        mockPost3.setCreatedAt(LocalDateTime.now().withNano(0));
        given(boardService.findBoardById(1L)).willReturn(mockBoard);
        given(postRepository.findPopularPostsByBoardId(pageable, mockBoard)).willReturn(postPage);
        given(postMetaService.getPostMetaInfos(anyList())).willReturn(dbMetaMap);

        // when
        Page<PostSummaryResponse> result =
                postService.getPopularPostsByBoardId(pageRequest, mockBoard.getId());

        // then
        List<PostSummaryResponse> list = result.stream().toList();
        assertThat(list.size()).isEqualTo(3);
    }

    @Test
    @DisplayName("게시판별 좋아요 30개 이상 게시글 조회")
    public void getMostLikedPostsByBoardId() {
        // given
        CustomPageRequest pageRequest = new CustomPageRequest(1, 10, "createdAt", "desc");
        Pageable pageable = pageRequest.toPageable();
        List<Post> posts = Arrays.asList(mockPost3);
        Page<Post> postPage = new PageImpl<>(posts, pageable, 1);
        postIdList.add(3L);
        dbMetaMap.put(postIdList.get(0), postMetaResponse3);
        given(boardService.findBoardById(1L)).willReturn(mockBoard);
        given(postRepository.findMostLikedPostsByBoardId(pageable, mockBoard)).willReturn(postPage);
        given(postMetaService.getPostMetaInfos(anyList())).willReturn(dbMetaMap);

        // when
        Page<PostSummaryResponse> result =
                postService.getMostLikedPostsByBoardId(pageRequest, mockBoard.getId());

        // then
        List<PostSummaryResponse> list = result.stream().toList();
        assertThat(list.size()).isEqualTo(1);
        assertThat(list.get(0).id()).isEqualTo(3);
        assertThat(list.getFirst().likeCount()).isEqualTo(postMeta3.getLikeCount());
        assertThat(list.getFirst().viewCount()).isEqualTo(postMeta3.getViewCount());
        assertThat(list.getFirst().commentCount()).isEqualTo(postMeta3.getCommentCount());
        assertThat(list.getFirst().likeCount()).isGreaterThanOrEqualTo(30);
    }
}
