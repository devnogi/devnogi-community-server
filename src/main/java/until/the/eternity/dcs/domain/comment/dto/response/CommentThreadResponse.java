package until.the.eternity.dcs.domain.comment.dto.response;

import java.util.List;

public record CommentThreadResponse(
        CommentPageResponseItem comment, List<CommentPageResponseItem> replies, long replyCount) {}
