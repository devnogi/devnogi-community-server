package until.the.eternity.dcs.domain.post.exception;

import static until.the.eternity.dcs.domain.post.exception.PostImageExceptionCode.INVALID_EXTENSION_EXCEPTION;

import until.the.eternity.dcs.common.exception.CustomException;

public class InvalidExtensionException extends CustomException {
    public InvalidExtensionException() {
        super(INVALID_EXTENSION_EXCEPTION);
    }
}
