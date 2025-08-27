package until.the.eternity.dcs.domain.comment.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import until.the.eternity.dcs.domain.comment.dto.request.CommentLikeToggleRequest;
import until.the.eternity.dcs.domain.comment.entity.CommentLike;

class CommentLikeConverterTest {
    CommentLikeConverter commentLikeConverter = new CommentLikeConverter();

    @Test
    @DisplayName("fromToggleRequest 는 CommentLikeToggleRequest 를 CommentLike 로 변환한다.")
    void fromToggleRequest_Success() {
        // given
        Long id = 1L;
        Long userId = 1L;
        CommentLikeToggleRequest request = new CommentLikeToggleRequest(id, userId);

        // when
        CommentLike commentLike = commentLikeConverter.fromToggleRequest(request, id);

        // then
        assertNotNull(commentLike);
        assertEquals(id, commentLike.getCommentId());
        assertEquals(id, commentLike.getUserId());
    }
}
