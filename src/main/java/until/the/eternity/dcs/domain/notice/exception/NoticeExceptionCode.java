package until.the.eternity.dcs.domain.notice.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import until.the.eternity.dcs.common.exception.ExceptionCode;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Getter
@RequiredArgsConstructor
public enum NoticeExceptionCode implements ExceptionCode {
    NOTICE_NOT_FOUND_EXCEPTION(NOT_FOUND, "해당 아이디의 알림은 존재하지 않습니다."),
    NOTICE_SEND_FORBIDDEN_EXCEPTION(FORBIDDEN, "알림은 관리자와 시스템만 발송할 수 있습니다."),
    ;

    private final HttpStatus status;
    private final String message;

    @Override
    public String getCode() {
        return this.name();
    }
}
