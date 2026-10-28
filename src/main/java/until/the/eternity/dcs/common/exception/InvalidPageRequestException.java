package until.the.eternity.dcs.common.exception;

import static until.the.eternity.dcs.common.exception.GlobalExceptionCode.INVALID_PAGE_REQUEST_EXCEPTION;

public class InvalidPageRequestException extends CustomException {
    public InvalidPageRequestException() {
        super(INVALID_PAGE_REQUEST_EXCEPTION);
    }
}
