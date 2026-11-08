package until.the.eternity.dcs.common.exception;

public class FileStorageException extends CustomException {
    public FileStorageException(ExceptionCode code) {
        super(code);
    }

    public FileStorageException(ExceptionCode code, Throwable cause) {
        super(code, cause);
    }
}
