package until.the.eternity.dcs.domain.announcement.exception;

import static until.the.eternity.dcs.domain.announcement.exception.AnnouncementExceptionCode.ANNOUNCEMENT_NOT_FOUND_EXCEPTION;

import until.the.eternity.dcs.common.exception.CustomException;

public class AnnouncementNotFoundException extends CustomException {
    public AnnouncementNotFoundException(Long id) {
        super(
                ANNOUNCEMENT_NOT_FOUND_EXCEPTION,
                ANNOUNCEMENT_NOT_FOUND_EXCEPTION.getMessage() + " ID : " + id);
    }
}
