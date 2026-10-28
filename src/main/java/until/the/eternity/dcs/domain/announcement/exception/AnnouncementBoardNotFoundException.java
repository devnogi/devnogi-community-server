package until.the.eternity.dcs.domain.announcement.exception;

import until.the.eternity.dcs.common.exception.CustomException;

public class AnnouncementBoardNotFoundException extends CustomException {
    public AnnouncementBoardNotFoundException(Long boardId) {
        super(
                AnnouncementExceptionCode.ANNOUNCEMENT_BOARD_NOT_FOUND_EXCEPTION,
                AnnouncementExceptionCode.ANNOUNCEMENT_BOARD_NOT_FOUND_EXCEPTION.getMessage()
                        + " ID : "
                        + boardId);
    }
}
