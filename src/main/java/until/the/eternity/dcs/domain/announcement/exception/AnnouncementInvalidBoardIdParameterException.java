package until.the.eternity.dcs.domain.announcement.exception;

import until.the.eternity.dcs.common.exception.CustomException;

public class AnnouncementInvalidBoardIdParameterException extends CustomException {

    public AnnouncementInvalidBoardIdParameterException() {
        super(AnnouncementExceptionCode.ANNOUNCEMENT_INVALID_BOARD_ID_EXCEPTION);
    }
}
