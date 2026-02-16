package until.the.eternity.dcs.domain.report.exception;

import until.the.eternity.dcs.common.exception.CustomException;

import static until.the.eternity.dcs.domain.report.exception.ReportExceptionCode.STATUS_NOT_FOUND_EXCEPTION;

public class StatusNotFoundException extends CustomException {
    public StatusNotFoundException(String message) {
        super(
                STATUS_NOT_FOUND_EXCEPTION,
                STATUS_NOT_FOUND_EXCEPTION.getMessage() + " STATUS : " + message);
    }
}
