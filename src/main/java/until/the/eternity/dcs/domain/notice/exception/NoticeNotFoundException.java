package until.the.eternity.dcs.domain.notice.exception;

import static until.the.eternity.dcs.domain.notice.exception.NoticeExceptionCode.NOTICE_NOT_FOUND_EXCEPTION;

import until.the.eternity.dcs.common.exception.CustomException;

public class NoticeNotFoundException extends CustomException {
    public NoticeNotFoundException(Long id) {
        super(NOTICE_NOT_FOUND_EXCEPTION, NOTICE_NOT_FOUND_EXCEPTION.getMessage() + " ID : " + id);
    }
}
