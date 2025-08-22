package until.the.eternity.dcs.domain.notice.exception;

import static org.springframework.http.HttpStatus.NOT_FOUND;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import until.the.eternity.dcs.common.exception.ExceptionCode;

@Getter
@RequiredArgsConstructor
public enum NoticeExceptionCode implements ExceptionCode {
    NOTICE_NOT_FOUND_EXCEPTION(NOT_FOUND, "해당 아이디의 알림은 존재하지 않습니다.");

    private final HttpStatus status;
    private final String message;

    @Override
    public String getCode() {
        return this.name();
    }
}
