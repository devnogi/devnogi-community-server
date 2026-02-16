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
    ANNOUNCEMENT_DUPLICATE_EXCEPTION(CONFLICT, "?대떦 寃뚯떆湲? ?대? 怨듭?濡??깅줉?섏뿀?듬땲??"),
    ANNOUNCEMENT_NOT_FOUND_EXCEPTION(NOT_FOUND, "?대떦 ?꾩씠?붿쓽 怨듭?湲? 議댁옱?섏? ?딆뒿?덈떎."),
    ANNOUNCEMENT_BOARD_NOT_FOUND_EXCEPTION(
            NOT_FOUND, "?대떦 寃뚯떆?먯씠 議댁옱?섏? ?딆뒿?덈떎."),
    ;

    private final HttpStatus status;
    private final String message;

    @Override
    public String getCode() {
        return this.name();
    }
}
