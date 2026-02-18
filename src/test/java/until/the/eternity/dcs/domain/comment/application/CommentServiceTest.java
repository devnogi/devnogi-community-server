package until.the.eternity.dcs.domain.comment.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static until.the.eternity.dcs.domain.user.enums.UserGrade.ADMIN;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import until.the.eternity.dcs.common.notification.RedisSender;
import until.the.eternity.dcs.common.request.CustomPageRequest;
import until.the.eternity.dcs.domain.comment.dto.request.CommentCreateRequest;
import until.the.eternity.dcs.domain.comment.dto.request.CommentLikeToggleRequest;
import until.the.eternity.dcs.domain.comment.dto.request.CommentUpdateRequest;
import until.the.eternity.dcs.domain.comment.dto.response.CommentPageResponseItem;
import until.the.eternity.dcs.domain.comment.dto.response.CommentPersistResponse;
import until.the.eternity.dcs.domain.comment.entity.Comment;
import until.the.eternity.dcs.domain.comment.entity.CommentLike;
import until.the.eternity.dcs.domain.comment.entity.CommentMeta;
import until.the.eternity.dcs.domain.comment.infrastructure.CommentLikeRepository;
import until.the.eternity.dcs.domain.comment.infrastructure.CommentMetaRepository;
import until.the.eternity.dcs.domain.comment.infrastructure.CommentRepository;
import until.the.eternity.dcs.domain.post.application.PostMetaService;
import until.the.eternity.dcs.domain.post.entity.Post;
import until.the.eternity.dcs.domain.post.entity.PostMeta;
import until.the.eternity.dcs.domain.post.infrastructure.PostMetaRepository;
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
    PostMetaRepository postMetaRepository = mock(PostMetaRepository.class);
    Long userId = 1L;
    RedisSender redisSender = mock(RedisSender.class);
    CommentPermissionEvaluator commentPermissionEvaluator = mock(CommentPermissionEvaluator.class);
    PostMetaService postMetaService = mock(PostMetaService.class);
    Integer commentCount = 1;

    CommentService commentService =
            new CommentService(
                    commentRepository,
                    commentConverter,
                    commentLikeRepository,
                    commentLikeConverter,
                    commentMetaRepository,
                    postRepository,
                    postMetaRepository,
                    redisSender,
                    commentPermissionEvaluator,
                    userService,
                    postMetaService);

    Comment comment;
    Long id = 1L;
    String content = "content";
    UserSummary user;
    CommentMeta commentMeta;

    @BeforeEach
    void init() {
        Post post = Post.builder().id(id).build();
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
        when(postMetaRepository.findById(id))
                .thenReturn(Optional.of(PostMeta.create(id, commentCount)));
        CommentCreateRequest request = new CommentCreateRequest(null, content);

        // when
        CommentPersistResponse response = commentService.create(id, request);

        // then
        assertNotNull(response);
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
        when(postMetaRepository.findById(id))
                .thenReturn(Optional.of(PostMeta.create(id, commentCount)));

        // when
        when(commentRepository.findById(anyLong())).thenReturn(Optional.of(comment));
        commentService.delete(id);

        // then
        Optional<Comment> comment = commentRepository.findById(id);
        assertTrue(comment.get().getIsDeleted());
    }

    @Test
    @DisplayName("findByPostId 는 postId 로 comment 를 페이징 조회한다. (로그아웃)")
    void findByPostId_Success() {
        // given
        Page<Comment> page = new PageImpl<>(List.of(comment));
        when(commentRepository.findAllByPostId(anyLong(), any(Pageable.class))).thenReturn(page);
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
