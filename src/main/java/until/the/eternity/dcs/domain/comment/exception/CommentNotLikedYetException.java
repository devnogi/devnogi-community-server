package until.the.eternity.dcs.domain.comment.exception;

import until.the.eternity.dcs.common.exception.CustomException;

import static until.the.eternity.dcs.domain.comment.exception.CommentExceptionCode.COMMENT_NOT_FOUND_EXCEPTION;
import static until.the.eternity.dcs.domain.comment.exception.CommentExceptionCode.COMMENT_NOT_LIKED_YET_EXCEPTION;

public class CommentNotLikedYetException extends CustomException {
    public CommentNotLikedYetException(Long commentId) {
        super(
                COMMENT_NOT_LIKED_YET_EXCEPTION,
                COMMENT_NOT_FOUND_EXCEPTION.getMessage() + " ID : " + commentId);
    }
}
