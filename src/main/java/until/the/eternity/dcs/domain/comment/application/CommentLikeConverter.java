package until.the.eternity.dcs.domain.comment.application;

import org.springframework.stereotype.Component;
import until.the.eternity.dcs.domain.comment.dto.request.CommentLikeToggleRequest;
import until.the.eternity.dcs.domain.comment.entity.CommentLike;

@Component
public class CommentLikeConverter {
    public CommentLike fromToggleRequest(CommentLikeToggleRequest request, Long userId) {
        return CommentLike.builder().userId(userId).commentId(request.commentId()).build();
    }
}
