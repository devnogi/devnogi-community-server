package until.the.eternity.dcs.domain.board.exception;

import static until.the.eternity.dcs.domain.board.exception.BoardExceptionCode.BOARD_MODIFY_FORBIDDEN_EXCEPTION;

import until.the.eternity.dcs.common.exception.CustomException;

public class BoardModifyForbiddenException extends CustomException {
    public BoardModifyForbiddenException() {
        super(BOARD_MODIFY_FORBIDDEN_EXCEPTION);
    }
}
