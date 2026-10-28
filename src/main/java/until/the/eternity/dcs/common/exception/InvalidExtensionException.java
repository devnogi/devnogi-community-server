package until.the.eternity.dcs.common.exception;

import static until.the.eternity.dcs.common.exception.PostImageExceptionCode.INVALID_EXTENSION_EXCEPTION;

public class InvalidExtensionException extends CustomException {
    public InvalidExtensionException() {
        super(INVALID_EXTENSION_EXCEPTION);
    }
}
