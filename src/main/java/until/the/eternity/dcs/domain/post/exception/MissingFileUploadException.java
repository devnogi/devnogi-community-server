package until.the.eternity.dcs.domain.post.exception;

import static until.the.eternity.dcs.domain.post.exception.PostImageExceptionCode.MISSING_FILE_UPLOAD_EXCEPTION;

import until.the.eternity.dcs.common.exception.CustomException;

public class MissingFileUploadException extends CustomException {
    public MissingFileUploadException() {
        super(MISSING_FILE_UPLOAD_EXCEPTION);
    }
}
