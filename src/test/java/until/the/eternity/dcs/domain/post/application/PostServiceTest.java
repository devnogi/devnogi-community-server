package until.the.eternity.dcs.domain.post.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import until.the.eternity.dcs.common.request.CustomPageRequest;
import until.the.eternity.dcs.domain.board.entity.Board;
import until.the.eternity.dcs.domain.comment.entity.Comment;
import until.the.eternity.dcs.domain.post.dto.request.PostCreateRequest;
import until.the.eternity.dcs.domain.post.dto.request.PostLikeCreateRequest;
import until.the.eternity.dcs.domain.post.dto.request.PostUpdateRequest;
import until.the.eternity.dcs.domain.post.dto.response.PostDetailResponse;
import until.the.eternity.dcs.domain.post.dto.response.PostPersistResponse;
import until.the.eternity.dcs.domain.post.dto.response.PostSummaryResponse;
import until.the.eternity.dcs.domain.post.entity.Post;
import until.the.eternity.dcs.domain.post.entity.PostMeta;
import until.the.eternity.dcs.domain.post.exception.PostDeletionNotAllowedException;
import until.the.eternity.dcs.domain.post.exception.PostModifyForbiddenException;
import until.the.eternity.dcs.domain.post.exception.PostNotFoundException;
import until.the.eternity.dcs.domain.post.infrastructure.PostLikeRepository;
import until.the.eternity.dcs.domain.post.infrastructure.PostMetaRepository;
import until.the.eternity.dcs.domain.post.infrastructure.PostRepository;
import until.the.eternity.dcs.domain.tag.application.PostTagService;
import until.the.eternity.dcs.domain.tag.application.TagService;
import until.the.eternity.dcs.domain.user.application.UserService;
import until.the.eternity.dcs.domain.user.entity.UserSummary;

@ExtendWith(MockitoExtension.class)
@DisplayName("PostService 단위 테스트")
class PostServiceTest {

    @Mock private PostRepository postRepository;

    @Mock private PostConverter postConverter;

    @Mock private UserService fakeUserService;

    @Mock private PostLikeRepository postLikeRepository;

    @Mock private PostLikeConverter postLikeConverter;

    @Mock private TagService tagService;

    @Mock private PostTagService postTagService;

    @InjectMocks private PostService postService;

    @Mock private PostMetaRepository postMetaRepository;
    private PostMeta postMeta;

    private UserSummary mockUser;
    private Post mockPost;
    private Post mockPost2;
    private PostCreateRequest createRequest;
    private PostUpdateRequest updateRequest;
    private PostSummaryResponse mockSummaryResponse;
    private PostDetailResponse mockDetailResponse;
    private PostPersistResponse mockPersistResponse;

    @BeforeEach
    void setUp() {
        mockUser = UserSummary.builder().id(1L).nickname("testUser").build();

        Board mockBoard = Board.builder().id(1L).build();

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

        mockPost2.setIsDeleted(false);

        createRequest =
                new PostCreateRequest(
                        1L, "New Post", "New Content", false, Arrays.asList("tag1", "tag2"));

        updateRequest =
                new PostUpdateRequest(
                        1L,
                        "Updated Title",
                        "Updated Content",
                        false,
                        Arrays.asList("tag3", "tag4"));

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
    }

    @Nested
    @DisplayName("게시글 생성 테스트")
    class CreatePostTest {

        @Test
        @DisplayName("정상적인 게시글 생성")
        void createPost_Success() {
            // Given
            given(fakeUserService.getCurrentUser()).willReturn(mockUser);
            given(postConverter.fromCreateRequestToPost(eq(createRequest), eq(1L)))
                    .willReturn(mockPost);
            given(postRepository.save(mockPost)).willReturn(mockPost);
            given(postConverter.fromPostToPostPersistResponse(mockPost))
                    .willReturn(mockPersistResponse);

            // When
            PostPersistResponse result = postService.createPost(createRequest);

            // Then
            assertThat(result).isEqualTo(mockPersistResponse);
            verify(fakeUserService).getCurrentUser();
            verify(postConverter).fromCreateRequestToPost(eq(createRequest), eq(1L));
            verify(postRepository).save(mockPost);
            verify(postConverter).fromPostToPostPersistResponse(mockPost);
        }

        @Test
        @DisplayName("사용자 인증 실패 시 예외 발생")
        void createPost_UserAuthenticationFailed() {
            // Given
            given(fakeUserService.getCurrentUser())
                    .willThrow(new RuntimeException("User not found"));

            // When & Then
            assertThatThrownBy(() -> postService.createPost(createRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("User not found");

            verify(fakeUserService).getCurrentUser();
            verify(postRepository, never()).save(any());
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
            given(postMetaRepository.findByPostId(1L)).willReturn(Optional.ofNullable(postMeta));
            given(postRepository.findByIdAndIsDeletedFalseAndIsBlockedFalse(postId))
                    .willReturn(Optional.of(postWithComments));
            given(postConverter.fromPostToPostDetailResponse(postWithComments, postMeta))
                    .willReturn(mockDetailResponse);
            int cnt = postMeta.getViewCount();
            // When
            PostDetailResponse result = postService.findPost(postId);

            // Then
            assertThat(result).isEqualTo(mockDetailResponse);
            assertThat(result.viewCount()).isEqualTo(cnt + 1);
            assertThat(result.viewCount()).isEqualTo(postMeta.getViewCount());
            verify(postRepository).findByIdAndIsDeletedFalseAndIsBlockedFalse(postId);
            verify(postConverter).fromPostToPostDetailResponse(postWithComments, postMeta);
        }

        @Test
        @DisplayName("존재하지 않는 게시글 조회 시 예외 발생")
        void findPost_NotFound() {
            // Given
            Long postId = 999L;
            given(postRepository.findByIdAndIsDeletedFalseAndIsBlockedFalse(postId))
                    .willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> postService.findPost(postId))
                    .isInstanceOf(PostNotFoundException.class);

            verify(postRepository).findByIdAndIsDeletedFalseAndIsBlockedFalse(postId);
        }

        @Test
        @DisplayName("게시글 목록 조회 성공")
        void findPosts_Success() {
            // Given
            CustomPageRequest pageRequest = mock(CustomPageRequest.class);

            Pageable pageable = PageRequest.of(1, 10);
            List<Post> posts = Arrays.asList(mockPost, mockPost2);
            Page<Post> postPage = new PageImpl<>(posts, pageable, 1);

            given(pageRequest.toPageable()).willReturn(pageable);
            given(postRepository.findAllByIsDeletedFalseAndIsBlockedFalse(pageable))
                    .willReturn(postPage);
            given(postMetaRepository.findByPostId(1L)).willReturn(Optional.ofNullable(postMeta));

            // When
            Page<PostSummaryResponse> result = postService.findPosts(pageRequest);

            // Then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent().get(0)).isEqualTo(mockSummaryResponse);
            verify(postRepository).findAllByIsDeletedFalseAndIsBlockedFalse(pageable);
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
            given(fakeUserService.getCurrentUser()).willReturn(mockUser);
            given(postRepository.findWithTagsById(postId)).willReturn(Optional.of(mockPost));
            given(postRepository.save(mockPost)).willReturn(mockPost);
            given(postConverter.fromPostToPostPersistResponse(mockPost))
                    .willReturn(mockPersistResponse);

            // When
            PostPersistResponse result = postService.updatePost(postId, updateRequest);

            // Then
            assertThat(result).isEqualTo(mockPersistResponse);
            verify(fakeUserService).getCurrentUser();
            verify(postRepository).findWithTagsById(postId);
            verify(postRepository).save(mockPost);
        }

        @Test
        @DisplayName("작성자가 아닌 사용자의 수정 시도 시 예외 발생")
        void updatePost_ForbiddenUser() {
            // Given
            Long postId = 1L;
            UserSummary anotherUser = UserSummary.builder().id(2L).nickname("anotherUser").build();

            given(fakeUserService.getCurrentUser()).willReturn(anotherUser);

            // When & Then
            assertThatThrownBy(() -> postService.updatePost(postId, updateRequest))
                    .isInstanceOf(PostModifyForbiddenException.class);

            verify(fakeUserService).getCurrentUser();
            verify(postRepository, never()).save(any());
        }

        @Test
        @DisplayName("존재하지 않는 게시글 수정 시 예외 발생")
        void updatePost_PostNotFound() {
            // Given
            Long postId = 999L;
            given(fakeUserService.getCurrentUser()).willReturn(mockUser);
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
            given(fakeUserService.getCurrentUser()).willReturn(mockUser);
            given(postRepository.findWithTagsById(postId)).willReturn(Optional.of(mockPost));

            // When
            postService.deletePost(postId);

            // Then
            verify(fakeUserService).getCurrentUser();
            verify(postRepository).findWithTagsById(postId);
            verify(postRepository).delete(mockPost);
        }

        @Test
        @DisplayName("작성자가 아닌 사용자의 삭제 시도 시 예외 발생")
        void deletePost_NotAllowed() {
            // Given
            Long postId = 1L;
            UserSummary anotherUser = UserSummary.builder().id(2L).nickname("anotherUser").build();

            given(fakeUserService.getCurrentUser()).willReturn(anotherUser);
            given(postRepository.findWithTagsById(postId)).willReturn(Optional.of(mockPost));

            // When & Then
            assertThatThrownBy(() -> postService.deletePost(postId))
                    .isInstanceOf(PostDeletionNotAllowedException.class);

            verify(fakeUserService).getCurrentUser();
            verify(postRepository).findWithTagsById(postId);
            verify(postRepository, never()).delete(any());
        }

        @Test
        @DisplayName("존재하지 않는 게시글 삭제 시 예외 발생")
        void deletePost_PostNotFound() {
            // Given
            Long postId = 999L;
            given(fakeUserService.getCurrentUser()).willReturn(mockUser);
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

            PostLikeCreateRequest postLikeCreateRequest =
                    new PostLikeCreateRequest(mockPost.getId());
            given(fakeUserService.getCurrentUser()).willReturn(mockUser);
            given(postLikeRepository.existsByUserIdAndPostId(mockUser.getId(), mockPost.getId()))
                    .willReturn(false);
            given(postRepository.findWithTagsById(1L)).willReturn(Optional.of(mockPost));
            given(postMetaRepository.findByPostId(1L)).willReturn(Optional.ofNullable(postMeta));
            // when
            postService.togglePostLike(postLikeCreateRequest);

            // then
            assertThat(postMeta.getLikeCount()).isEqualTo(1);
            verify(postLikeRepository).existsByUserIdAndPostId(mockUser.getId(), mockPost.getId());
        }

        @Test
        @DisplayName("게시글 좋아요 해제")
        public void unlikePost_Test() {
            // given
            PostLikeCreateRequest postLikeCreateRequest =
                    new PostLikeCreateRequest(mockPost.getId());

            given(postLikeRepository.existsByUserIdAndPostId(mockUser.getId(), mockPost.getId()))
                    .willReturn(true);
            given(fakeUserService.getCurrentUser()).willReturn(mockUser);
            given(postRepository.findWithTagsById(1L)).willReturn(Optional.of(mockPost));
            given(postMetaRepository.findByPostId(1L)).willReturn(Optional.ofNullable(postMeta));

            // when
            postService.togglePostLike(postLikeCreateRequest);

            // then
            assertThat(postMeta.getLikeCount()).isEqualTo(-1);
            verify(postRepository).findWithTagsById(1L);
            verify(postLikeRepository).existsByUserIdAndPostId(mockUser.getId(), mockPost.getId());
            verify(postLikeRepository).deleteByUserIdAndPostId(mockUser.getId(), mockPost.getId());
        }
    }
}
