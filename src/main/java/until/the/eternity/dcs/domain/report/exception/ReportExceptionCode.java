package until.the.eternity.dcs.domain.report.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import until.the.eternity.dcs.common.exception.ExceptionCode;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Getter
@RequiredArgsConstructor
public enum ReportExceptionCode implements ExceptionCode {
    REPORT_NOT_FOUND_EXCEPTION(NOT_FOUND, "해당 신고를 찾을 수 없습니다."),
    STATUS_NOT_FOUND_EXCEPTION(NOT_FOUND, "해당 상태를 찾을 수 없습니다."),
    REPORT_MODIFY_FORBIDDEN_EXCEPTION(FORBIDDEN, "수정 권한이 부족합니다."),
    TARGET_NOT_FOUND_EXCEPTION(NOT_FOUND, "해당 대상을 찾을 수 없습니다."),
    CATEGORY_NOT_FOUND_EXCEPTION(NOT_FOUND, "해당 카테고리를 찾을 수 없습니다");
    private final HttpStatus status;
    private final String message;

    @Override
    public String getCode() {
        return this.name();
    }
}
