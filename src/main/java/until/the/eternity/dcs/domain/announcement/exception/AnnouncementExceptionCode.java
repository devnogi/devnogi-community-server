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
    ANNOUNCEMENT_DUPLICATE_EXCEPTION(CONFLICT, "해당 게시물이 이미 공지로 등록되어 있습니다."),
    ANNOUNCEMENT_NOT_FOUND_EXCEPTION(NOT_FOUND, "해당 ID의 공지글이 존재하지 않습니다."),
    ANNOUNCEMENT_BOARD_NOT_FOUND_EXCEPTION(
            NOT_FOUND, "해당 게시판이 존재하지 않습니다."),
    ;

    private final HttpStatus status;
    private final String message;

    @Override
    public String getCode() {
        return this.name();
    }
}
