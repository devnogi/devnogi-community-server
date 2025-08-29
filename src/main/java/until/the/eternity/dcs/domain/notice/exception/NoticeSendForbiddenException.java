package until.the.eternity.dcs.domain.notice.exception;

import static until.the.eternity.dcs.domain.notice.exception.NoticeExceptionCode.NOTICE_SEND_FORBIDDEN_EXCEPTION;

import until.the.eternity.dcs.common.exception.CustomException;

public class NoticeSendForbiddenException extends CustomException {
    public NoticeSendForbiddenException() {
        super(NOTICE_SEND_FORBIDDEN_EXCEPTION);
    }
}
