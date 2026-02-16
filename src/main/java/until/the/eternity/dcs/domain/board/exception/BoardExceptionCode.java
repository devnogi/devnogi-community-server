package until.the.eternity.dcs.domain.board.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import until.the.eternity.dcs.common.exception.ExceptionCode;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Getter
@RequiredArgsConstructor
public enum BoardExceptionCode implements ExceptionCode {
    BOARD_NOT_FOUND_EXCEPTION(NOT_FOUND, "해당 게시판을 찾을 수 없습니다."),
    BOARD_MODIFY_FORBIDDEN_EXCEPTION(FORBIDDEN, "관리자 권한의 유저만 게시판을 편집할 수 있습니다."),
    ;

    private final HttpStatus status;
    private final String message;

    @Override
    public String getCode() {
        return this.name();
    }
}
