package until.the.eternity.dcs.domain.comment.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import until.the.eternity.dcs.domain.comment.dto.request.CommentCreateRequest;
import until.the.eternity.dcs.domain.comment.dto.response.CommentPageResponseItem;
import until.the.eternity.dcs.domain.comment.dto.response.CommentPersistResponse;
import until.the.eternity.dcs.domain.comment.entity.Comment;
import until.the.eternity.dcs.domain.comment.infrastructure.CommentRepository;
import until.the.eternity.dcs.domain.post.entity.Post;
import until.the.eternity.dcs.domain.post.infrastructure.PostRepository;

class CommentConverterTest {
    CommentRepository commentRepository = mock(CommentRepository.class);
    PostRepository postRepository = mock(PostRepository.class);
    CommentConverter commentConverter = new CommentConverter(commentRepository, postRepository);

    Comment comment;
    Long id = 1L;
    String content = "content";
    Long userId = 2L;
    String username = "username";

    @BeforeEach
    void init() {
        comment = Comment.builder().id(id).content(content).build();
    }

    @Test
    @DisplayName("CommentCreateRequest 에서 Comment 로 변환할 수 있다.")
    void fromCreateRequestToComment_Success() {
        // given
        when(postRepository.findByIdAndIsDeletedFalseAndIsBlockedFalse(id))
                .thenReturn(Optional.of(Post.builder().id(id).build()));
        CommentCreateRequest request = new CommentCreateRequest(null, content);

        // when
        Comment comment = commentConverter.fromCreateRequestToComment(request, userId, id);

        // then
        assertNotNull(comment);
        assertEquals(content, comment.getContent());
        assertEquals(id, comment.getPost().getId());
        assertEquals(userId, comment.getUserId());
    }

    @Test
    @DisplayName("CommentCreateRequest 에서 Comment 로 parentId와 함께 변환할 수 있다.")
    void fromCreateRequestToComment_parentId() {
        // given
        Post post = Post.builder().id(id).build();
        comment = Comment.builder().id(id).content(content).post(post).build();
        when(commentRepository.findById(id)).thenReturn(Optional.of(comment));
        when(postRepository.findByIdAndIsDeletedFalseAndIsBlockedFalse(id))
                .thenReturn(Optional.of(post));
        CommentCreateRequest request = new CommentCreateRequest(id, content);

        // when
        Comment comment = commentConverter.fromCreateRequestToComment(request, userId, id);

        // then
        assertNotNull(comment);
        assertEquals(id, comment.getParentComment().getId());
    }

    @Test
    void rejectsParentFromAnotherPost() {
        Post requestedPost = Post.builder().id(1L).build();
        Comment foreignParent =
                Comment.builder().id(2L).post(Post.builder().id(99L).build()).build();
        when(postRepository.findByIdAndIsDeletedFalseAndIsBlockedFalse(1L))
                .thenReturn(Optional.of(requestedPost));
        when(commentRepository.findById(2L)).thenReturn(Optional.of(foreignParent));

        assertThatThrownBy(
                        () ->
                                commentConverter.fromCreateRequestToComment(
                                        new CommentCreateRequest(2L, content), userId, 1L))
                .isInstanceOf(
                        until.the.eternity.dcs.domain.comment.exception.CommentNotFoundException
                                .class);
    }

    @Test
    void rejectsReplyToReply() {
        Post post = Post.builder().id(1L).build();
        Comment root = Comment.builder().id(2L).post(post).build();
        Comment reply = Comment.builder().id(3L).post(post).parentComment(root).build();
        when(postRepository.findByIdAndIsDeletedFalseAndIsBlockedFalse(1L))
                .thenReturn(Optional.of(post));
        when(commentRepository.findById(3L)).thenReturn(Optional.of(reply));

        assertThatThrownBy(
                        () ->
                                commentConverter.fromCreateRequestToComment(
                                        new CommentCreateRequest(3L, content), userId, 1L))
                .isInstanceOf(
                        until.the.eternity.dcs.domain.comment.exception.CommentNotFoundException
                                .class);
    }

    @Test
    @DisplayName("Comment 에서 CommentPersistResponse 로 변환할 수 있다.")
    void fromCommentToPersistResponse_Success() {
        // given
        Comment comment = Comment.builder().id(id).content(content).build();

        // when
        CommentPersistResponse response = commentConverter.fromCommentToPersistResponse(comment);

        // then
        assertNotNull(response);
        assertEquals(id, response.id());
    }

    @Test
    @DisplayName("Comment 에서 CommentPageResponseItem 로 변환할 수 있다.")
    void fromCommentToPageResponse_Success() {
        // given
        Comment comment =
                Comment.builder()
                        .id(id)
                        .userId(userId)
                        .parentComment(null)
                        .content(content)
                        .build();

        // when
        CommentPageResponseItem response =
                commentConverter.fromCommentToPageResponse(comment, false, 0, username);

        // then
        assertNotNull(response);
        assertEquals(id, response.id());
        assertEquals(userId, response.userId());
        assertNull(response.parentComment());
        assertEquals(content, response.content());
        assertEquals(0, response.likeCount());
    }
}
