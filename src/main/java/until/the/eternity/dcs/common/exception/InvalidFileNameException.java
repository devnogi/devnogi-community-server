package until.the.eternity.dcs.common.exception;

import static until.the.eternity.dcs.common.exception.PostImageExceptionCode.INVALID_FILE_NAME_EXCEPTION;

public class InvalidFileNameException extends CustomException {
    public InvalidFileNameException() {
        super(INVALID_FILE_NAME_EXCEPTION);
    }
}
