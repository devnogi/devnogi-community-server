package until.the.eternity.dcs.domain.comment.exception;

import until.the.eternity.dcs.common.exception.CustomException;

import static until.the.eternity.dcs.domain.comment.exception.CommentExceptionCode.COMMENT_NOT_FOUND_EXCEPTION;

public class CommentNotFoundException extends CustomException {
	public CommentNotFoundException(Long id) {
		super(COMMENT_NOT_FOUND_EXCEPTION,  COMMENT_NOT_FOUND_EXCEPTION.getMessage() + " ID : " + id);
	}
}
