package until.the.eternity.dcs.domain.notice.exception;

import until.the.eternity.dcs.common.exception.CustomException;

import static until.the.eternity.dcs.domain.notice.exception.NoticeExceptionCode.NOTICE_SEND_FORBIDDEN_EXCEPTION;

public class NoticeSendForbiddenException extends CustomException {
    public NoticeSendForbiddenException() {
        super(NOTICE_SEND_FORBIDDEN_EXCEPTION);
    }
}
