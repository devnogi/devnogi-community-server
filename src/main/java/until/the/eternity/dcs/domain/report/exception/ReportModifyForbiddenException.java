package until.the.eternity.dcs.domain.report.exception;

import until.the.eternity.dcs.common.exception.CustomException;

import static until.the.eternity.dcs.domain.report.exception.ReportExceptionCode.REPORT_MODIFY_FORBIDDEN_EXCEPTION;

public class ReportModifyForbiddenException extends CustomException {
    public ReportModifyForbiddenException() {
        super(REPORT_MODIFY_FORBIDDEN_EXCEPTION, REPORT_MODIFY_FORBIDDEN_EXCEPTION.getMessage());
    }
}
