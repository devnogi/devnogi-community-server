package until.the.eternity.dcs.domain.comment.dto.request;

public record CommentCreateRequest(
	Long parentComment,

	String content
) {
}
