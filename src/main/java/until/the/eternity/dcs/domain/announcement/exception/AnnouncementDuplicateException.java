package until.the.eternity.dcs.domain.announcement.exception;

import until.the.eternity.dcs.common.exception.CustomException;

public class AnnouncementDuplicateException extends CustomException {
    public AnnouncementDuplicateException() {
        super(AnnouncementExceptionCode.ANNOUNCEMENT_DUPLICATE_EXCEPTION);
    }
}
