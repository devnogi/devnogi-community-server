package until.the.eternity.dcs.domain.report.exception;

import until.the.eternity.dcs.common.exception.CustomException;

import static until.the.eternity.dcs.domain.report.exception.ReportExceptionCode.REPORT_NOT_FOUND_EXCEPTION;

public class ReportNotFoundException extends CustomException {
    public ReportNotFoundException(Long id) {
        super(REPORT_NOT_FOUND_EXCEPTION, REPORT_NOT_FOUND_EXCEPTION.getMessage() + " ID : " + id);
    }
}
