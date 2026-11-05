package until.the.eternity.dcs.domain.comment.infrastructure;

public interface CommentReplyCount {
    Long getParentId();

    long getReplyCount();
}
