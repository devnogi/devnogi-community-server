package until.the.eternity.dcs.domain.board.exception;

import until.the.eternity.dcs.common.exception.CustomException;

import static until.the.eternity.dcs.domain.board.exception.BoardExceptionCode.BOARD_NOT_FOUND_EXCEPTION;

public class BoardNotFoundException extends CustomException {
    public BoardNotFoundException(Long id) {
        super(BOARD_NOT_FOUND_EXCEPTION, BOARD_NOT_FOUND_EXCEPTION.getMessage() + " ID : " + id);
    }
}
