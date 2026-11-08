package until.the.eternity.dcs.domain.post.exception;

public class PostMetaInvariantViolationException extends IllegalStateException {
    public PostMetaInvariantViolationException(Long postId) {
        super("Post metadata is missing for postId=" + postId);
    }
}
