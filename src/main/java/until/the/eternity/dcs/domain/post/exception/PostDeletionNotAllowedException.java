package until.the.eternity.dcs.domain.post.exception;

import until.the.eternity.dcs.common.exception.CustomException;

import static until.the.eternity.dcs.domain.post.exception.PostExceptionCode.POST_DELETION_NOT_ALLOWED_EXCEPTION;

public class PostDeletionNotAllowedException extends CustomException {
    public PostDeletionNotAllowedException() {
        super(POST_DELETION_NOT_ALLOWED_EXCEPTION);
    }
}
