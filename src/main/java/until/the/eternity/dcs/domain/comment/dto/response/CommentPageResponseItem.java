package until.the.eternity.dcs.domain.comment.dto.response;

import lombok.Builder;

@Builder
public record CommentPageResponseItem(
	Long id,

	Long createdBy,

	Long parentComment,

	String content,

	Integer likeCount
) {
}