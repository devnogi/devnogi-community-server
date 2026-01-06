package until.the.eternity.dcs.common.exception;

import static until.the.eternity.dcs.common.exception.PostImageExceptionCode.MISSING_FILE_UPLOAD_EXCEPTION;

public class MissingFileUploadException extends CustomException {
    public MissingFileUploadException() {
        super(MISSING_FILE_UPLOAD_EXCEPTION);
    }
}
