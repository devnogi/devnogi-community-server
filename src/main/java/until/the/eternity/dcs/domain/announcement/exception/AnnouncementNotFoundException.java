package until.the.eternity.dcs.domain.announcement.exception;

import until.the.eternity.dcs.common.exception.CustomException;

import static until.the.eternity.dcs.domain.announcement.exception.AnnouncementExceptionCode.ANNOUNCEMENT_NOT_FOUND_EXCEPTION;

public class AnnouncementNotFoundException extends CustomException {
    public AnnouncementNotFoundException(Long id) {
        super(
                ANNOUNCEMENT_NOT_FOUND_EXCEPTION,
                ANNOUNCEMENT_NOT_FOUND_EXCEPTION.getMessage() + " ID : " + id);
    }
}
