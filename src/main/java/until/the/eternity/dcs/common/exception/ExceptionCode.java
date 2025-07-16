package until.the.eternity.dcs.common.exception;

import org.springframework.http.HttpStatus;

public interface ExceptionCode {
    HttpStatus getStatus();

    String getCode();

    String getMessage();
}
