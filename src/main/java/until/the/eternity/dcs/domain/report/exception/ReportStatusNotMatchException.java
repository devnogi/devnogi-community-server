package until.the.eternity.dcs.domain.report.exception;

import static until.the.eternity.dcs.domain.report.exception.ReportExceptionCode.STATUS_NOT_MATCH_EXCEPTION;

import until.the.eternity.dcs.common.exception.CustomException;
import until.the.eternity.dcs.domain.report.enums.ReportStatus;

public class ReportStatusNotMatchException extends CustomException {
    public ReportStatusNotMatchException(ReportStatus reportStatus) {
        super(
                STATUS_NOT_MATCH_EXCEPTION,
                STATUS_NOT_MATCH_EXCEPTION.getMessage() + " - 요청한 신고의 상태: " + reportStatus);
    }
}
