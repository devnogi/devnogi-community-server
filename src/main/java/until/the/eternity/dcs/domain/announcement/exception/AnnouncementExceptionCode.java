package until.the.eternity.dcs.domain.announcement.exception;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import until.the.eternity.dcs.common.exception.ExceptionCode;

@Getter
@RequiredArgsConstructor
public enum AnnouncementExceptionCode implements ExceptionCode {
    ANNOUNCEMENT_DUPLICATE_EXCEPTION(CONFLICT, "해당 게시글은 이미 공지로 등록되었습니다."),
    ANNOUNCEMENT_NOT_FOUND_EXCEPTION(NOT_FOUND, "해당 아이디의 공지글은 존재하지 않습니다."),
    ;

    private final HttpStatus status;
    private final String message;

    @Override
    public String getCode() {
        return this.name();
    }
}
