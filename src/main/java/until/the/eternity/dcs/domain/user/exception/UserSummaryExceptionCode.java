package until.the.eternity.dcs.domain.user.exception;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import until.the.eternity.dcs.common.exception.ExceptionCode;

@Getter
@RequiredArgsConstructor
public enum UserSummaryExceptionCode implements ExceptionCode {
    USER_ALREADY_EXISTS_EXCEPTION(CONFLICT, "이미 등록된 사용자 입니다."),
    USER_NOT_FOUND_EXCEPTION(NOT_FOUND, "해당 사용자를 찾을 수 없습니다."),
    USER_GRADE_NOT_FOUND_EXCEPTION(NOT_FOUND, "해당 등급을 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String message;

    @Override
    public String getCode() {
        return this.name();
    }
}
