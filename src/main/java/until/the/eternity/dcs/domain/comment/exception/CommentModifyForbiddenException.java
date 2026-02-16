package until.the.eternity.dcs.domain.comment.exception;

import until.the.eternity.dcs.common.exception.CustomException;

import static until.the.eternity.dcs.domain.comment.exception.CommentExceptionCode.COMMENT_MODIFY_FORBIDDEN_EXCEPTION;

public class CommentModifyForbiddenException extends CustomException {
    public CommentModifyForbiddenException() {
        super(COMMENT_MODIFY_FORBIDDEN_EXCEPTION);
    }
}
