package until.the.eternity.dcs.common.exception;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum GlobalExceptionCode implements ExceptionCode {
    INVALID_PAGE_REQUEST_EXCEPTION(BAD_REQUEST, "페이지 요청 값이 유효하지 않습니다."),
    AUTHENTICATION_REQUIRED(UNAUTHORIZED, "인증이 필요합니다."),
    ACCESS_DENIED(FORBIDDEN, "요청한 작업을 수행할 권한이 없습니다."),
    SERVER_ERROR(INTERNAL_SERVER_ERROR, "서버 내부 에러입니다."),
    ;

    private final HttpStatus status;
    private final String message;

    @Override
    public String getCode() {
        return this.name();
    }
}
