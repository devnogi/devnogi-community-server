package until.the.eternity.dcs.domain.post.exception;

import static until.the.eternity.dcs.domain.post.exception.PostImageExceptionCode.INVALID_FILE_NAME_EXCEPTION;

import until.the.eternity.dcs.common.exception.CustomException;

public class InvalidFileNameException extends CustomException {
    public InvalidFileNameException() {
        super(INVALID_FILE_NAME_EXCEPTION);
    }
}
