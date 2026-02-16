package until.the.eternity.dcs.domain.comment.application;

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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
        when(commentRepository.findById(id)).thenReturn(Optional.of(comment));
        when(postRepository.findByIdAndIsDeletedFalseAndIsBlockedFalse(id))
                .thenReturn(Optional.of(Post.builder().id(id).build()));
        CommentCreateRequest request = new CommentCreateRequest(id, content);

        // when
        Comment comment = commentConverter.fromCreateRequestToComment(request, userId, id);

        // then
        assertNotNull(comment);
        assertEquals(id, comment.getParentComment().getId());
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
