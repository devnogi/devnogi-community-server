package until.the.eternity.dcs.domain.comment.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static until.the.eternity.dcs.domain.user.enums.UserGrade.ADMIN;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import until.the.eternity.dcs.common.notification.RedisSender;
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
import until.the.eternity.dcs.domain.comment.infrastructure.CommentLikeRepository;
import until.the.eternity.dcs.domain.comment.infrastructure.CommentMetaRepository;
import until.the.eternity.dcs.domain.comment.infrastructure.CommentReplyCount;
import until.the.eternity.dcs.domain.comment.infrastructure.CommentRepository;
import until.the.eternity.dcs.domain.post.application.PostMetaService;
import until.the.eternity.dcs.domain.post.entity.Post;
import until.the.eternity.dcs.domain.post.infrastructure.PostRepository;
import until.the.eternity.dcs.domain.user.application.UserSummaryService;
import until.the.eternity.dcs.domain.user.entity.UserSummary;

class CommentServiceTest {
    CommentRepository commentRepository = mock(CommentRepository.class);
    CommentLikeRepository commentLikeRepository = mock(CommentLikeRepository.class);
    PostRepository postRepository = mock(PostRepository.class);
    CommentConverter commentConverter = new CommentConverter(commentRepository, postRepository);
    UserSummaryService userService = mock(UserSummaryService.class);
    CommentLikeConverter commentLikeConverter = new CommentLikeConverter();
    CommentMetaRepository commentMetaRepository = mock(CommentMetaRepository.class);
    Long userId = 1L;
    RedisSender redisSender = mock(RedisSender.class);
    CommentPermissionEvaluator commentPermissionEvaluator = mock(CommentPermissionEvaluator.class);
    PostMetaService postMetaService = mock(PostMetaService.class);
    ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);

    CommentService commentService =
            new CommentService(
                    commentRepository,
                    commentConverter,
                    commentLikeRepository,
                    commentLikeConverter,
                    commentMetaRepository,
                    postRepository,
                    redisSender,
                    commentPermissionEvaluator,
                    userService,
                    postMetaService,
                    eventPublisher);

    Comment comment;
    Long id = 1L;
    String content = "content";
    UserSummary user;
    CommentMeta commentMeta;

    @BeforeEach
    void init() {
        Post post = Post.builder().id(id).title("post title").build();
        comment =
                Comment.builder()
                        .id(id)
                        .post(post)
                        .userId(id)
                        .parentComment(null)
                        .childComments(new ArrayList<>())
                        .content(content)
                        .build();

        user = UserSummary.builder().id(1L).grade(ADMIN).build();

        commentMeta = CommentMeta.create(id);
    }

    @Test
    @DisplayName("create 는 새로운 comment 를 생성힌다.")
    void create_Success() {
        // given
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);
        when(postRepository.findByIdAndIsDeletedFalseAndIsBlockedFalse(id))
                .thenReturn(Optional.of(Post.builder().id(id).comments(new ArrayList<>()).build()));
        CommentCreateRequest request = new CommentCreateRequest(null, content);

        // when
        CommentPersistResponse response = commentService.create(id, request);

        // then
        assertNotNull(response);
        verify(postMetaService).addComment(id);
        verify(eventPublisher).publishEvent(any(CommentCreatedNotification.class));
        verify(redisSender, never()).enqueue(any());
    }

    @Test
    @DisplayName("update 는 comment 의 content 를 수정한다.")
    void update_Success() {
        // given
        when(commentRepository.findById(anyLong())).thenReturn(Optional.of(comment));
        String newContent = "new content";
        CommentUpdateRequest request = new CommentUpdateRequest(newContent);

        // when
        CommentPersistResponse response = commentService.update(id, request);

        // then
        assertNotNull(response);
        assertEquals(id, response.id());
        Comment comment = commentRepository.findById(id).get();
        assertEquals(newContent, comment.getContent());
    }

    @Test
    @DisplayName("delete 는 comment 를 삭제 처리한다.")
    void delete_Success() {
        // given
        when(postRepository.findByIdAndIsDeletedFalseAndIsBlockedFalse(id))
                .thenReturn(Optional.of(Post.builder().id(id).comments(new ArrayList<>()).build()));

        // when
        when(commentRepository.findById(anyLong())).thenReturn(Optional.of(comment));
        commentService.delete(id);

        // then
        Optional<Comment> comment = commentRepository.findById(id);
        assertTrue(comment.get().getIsDeleted());
        verify(postMetaService).deleteComment(id);
    }

    @Test
    @DisplayName("findByPostId 는 postId 로 comment 를 페이징 조회한다. (로그아웃)")
    void findByPostId_Success() {
        // given
        Page<Comment> page = new PageImpl<>(List.of(comment));
        when(commentRepository.findAllByPostId(anyLong(), any(Pageable.class))).thenReturn(page);
        when(userService.findByIdIn(List.of(userId)))
                .thenReturn(
                        List.of(
                                UserSummary.builder()
                                        .id(userId)
                                        .nickname("user")
                                        .profileImageUrl("https://cdn.example/profile.png")
                                        .serverName("하프")
                                        .verified(true)
                                        .build()));
        CustomPageRequest request = new CustomPageRequest(1, 10, "createdAt", "desc");

        // when
        Page<CommentPageResponseItem> response = commentService.findByPostId(id, request);

        // then
        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals(1, response.getTotalPages());
        assertEquals(1, response.getNumberOfElements());
        assertEquals(1, response.getContent().size());
        assertEquals(content, response.getContent().get(0).content());
        assertEquals(
                "https://cdn.example/profile.png", response.getContent().get(0).profileImageUrl());
        assertEquals("하프", response.getContent().get(0).serverName());
        assertTrue(response.getContent().get(0).verified());
    }

    @Test
    @DisplayName("findByPostId 는 postId 로 comment 를 페이징 조회한다. (로그인)")
    void findByPostId_Success_Login() {
        // given
        Page<Comment> page = new PageImpl<>(List.of(comment));
        when(commentRepository.findAllByPostId(anyLong(), any(Pageable.class))).thenReturn(page);
        when(commentMetaRepository.findById(anyLong()))
                .thenReturn(Optional.of(CommentMeta.create(id)));
        when(userService.existsUserSummaryById(1L)).thenReturn(true);
        CustomPageRequest request = new CustomPageRequest(1, 10, "createdAt", "desc");

        // when
        Page<CommentPageResponseItem> response = commentService.findByPostId(id, request);

        // then
        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals(1, response.getTotalPages());
        assertEquals(1, response.getNumberOfElements());
        assertEquals(1, response.getContent().size());
        assertEquals(content, response.getContent().get(0).content());
        assertNull(response.getContent().get(0).profileImageUrl());
    }

    @Test
    void findThreadsBatchLoadsRepliesMetadataUsersAndLikedState() {
        Post post = Post.builder().id(id).build();
        Comment parent = Comment.builder().id(10L).post(post).userId(1L).content("parent").build();
        List<Comment> replies = new ArrayList<>();
        for (long replyId = 11; replyId <= 14; replyId++) {
            replies.add(
                    Comment.builder()
                            .id(replyId)
                            .post(post)
                            .parentComment(parent)
                            .userId(1L)
                            .content("reply-" + replyId)
                            .build());
        }
        when(commentRepository.findAllByPostIdAndParentCommentIsNull(eq(id), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(parent)));
        when(commentRepository.findFirst3ReplyIdsByPostIdAndParentIds(id, List.of(10L)))
                .thenReturn(List.of(11L, 12L, 13L));
        when(commentRepository.findAllByIdIn(List.of(11L, 12L, 13L)))
                .thenReturn(replies.subList(0, 3));
        CommentReplyCount replyCount = mock(CommentReplyCount.class);
        when(replyCount.getParentId()).thenReturn(10L);
        when(replyCount.getReplyCount()).thenReturn(4L);
        when(commentRepository.countRepliesByPostIdAndParentIds(id, List.of(10L)))
                .thenReturn(List.of(replyCount));
        when(userService.findByIdIn(List.of(1L)))
                .thenReturn(
                        List.of(
                                UserSummary.builder()
                                        .id(1L)
                                        .nickname("user")
                                        .serverName("류트")
                                        .verified(true)
                                        .build()));
        when(commentMetaRepository.findByCommentIdIn(List.of(10L, 11L, 12L, 13L)))
                .thenReturn(List.of());
        when(commentLikeRepository.findIdsByUserIdAndCommentIdIn(99L, List.of(10L, 11L, 12L, 13L)))
                .thenReturn(Set.of(11L));
        SecurityContextHolder.getContext()
                .setAuthentication(new TestingAuthenticationToken(99L, null, "ROLE_USER"));

        Page<CommentThreadResponse> result =
                commentService.findThreads(id, new CustomPageRequest(1, 20, null, null));

        assertEquals(4, result.getContent().getFirst().replyCount());
        assertEquals(3, result.getContent().getFirst().replies().size());
        assertTrue(result.getContent().getFirst().replies().getFirst().isLiked());
        assertEquals("류트", result.getContent().getFirst().comment().serverName());
        assertTrue(result.getContent().getFirst().comment().verified());
        assertEquals("류트", result.getContent().getFirst().replies().getFirst().serverName());
        assertTrue(result.getContent().getFirst().replies().getFirst().verified());
        verify(userService, times(1)).findByIdIn(List.of(1L));
        verify(commentMetaRepository, times(1)).findByCommentIdIn(List.of(10L, 11L, 12L, 13L));
        verify(commentLikeRepository, times(1))
                .findIdsByUserIdAndCommentIdIn(99L, List.of(10L, 11L, 12L, 13L));
        verify(commentRepository).findAllByIdIn(List.of(11L, 12L, 13L));
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("findByUserId 는 공개 댓글 활동과 게시글 이동 정보를 반환한다")
    void findByUserIdReturnsPublicActivity() {
        comment.setCreatedAt(java.time.LocalDateTime.of(2026, 11, 5, 12, 0));
        Page<Comment> page = new PageImpl<>(List.of(comment));
        when(commentRepository.findVisibleByUserId(eq(userId), any(Pageable.class)))
                .thenReturn(page);
        CommentMeta meta = CommentMeta.create(id);
        meta.like();
        meta.like();
        when(commentMetaRepository.findByCommentIdIn(List.of(id))).thenReturn(List.of(meta));
        when(userService.findByIdIn(List.of(userId)))
                .thenReturn(
                        List.of(
                                UserSummary.builder()
                                        .id(userId)
                                        .nickname("user")
                                        .serverName("만돌린")
                                        .verified(true)
                                        .build()));

        Page<UserCommentActivityResponse> response =
                commentService.findByUserId(userId, new UserCommentPageRequest(1, 20));

        assertEquals(1, response.getTotalElements());
        UserCommentActivityResponse item = response.getContent().getFirst();
        assertEquals(id, item.id());
        assertEquals(id, item.postId());
        assertEquals("post title", item.postTitle());
        assertEquals(content, item.content());
        assertEquals(2, item.likeCount());
        assertEquals("만돌린", item.serverName());
        assertTrue(item.verified());
        assertEquals(comment.getCreatedAt(), item.createdAt());
    }

    @Test
    @DisplayName("toggleLike 는 좋아요를 누르지 않았을 때 Comment 에 좋아요를 누른다. ")
    void toggleLike_Like() {
        // given
        when(commentRepository.findById(anyLong())).thenReturn(Optional.of(comment));
        when(userService.existsUserSummaryById(1L)).thenReturn(true);
        when(commentMetaRepository.findById(anyLong())).thenReturn(Optional.of(commentMeta));
        CommentLikeToggleRequest request = new CommentLikeToggleRequest(id);

        // when
        commentService.toggleLike(request);

        // then
        assertEquals(1, commentMeta.getLikeCount());
    }

    @Test
    @DisplayName("toggleLike 는 좋아요가 이미 눌려있을 때 Comment 에 좋아요를 취소한다. ")
    void toggleLike_Unlike() {
        // given
        commentMeta.like();
        CommentLike commentLike = CommentLike.builder().id(id).commentId(id).userId(id).build();
        when(commentRepository.findById(anyLong())).thenReturn(Optional.of(comment));
        when(commentLikeRepository.findByCommentIdAndUserId(anyLong(), anyLong()))
                .thenReturn(Optional.of(commentLike));
        when(userService.existsUserSummaryById(1L)).thenReturn(true);
        when(commentMetaRepository.findById(anyLong())).thenReturn(Optional.of(commentMeta));
        CommentLikeToggleRequest request = new CommentLikeToggleRequest(id);

        // when
        commentService.toggleLike(request);

        // then
        assertEquals(0, commentMeta.getLikeCount());
    }
}
