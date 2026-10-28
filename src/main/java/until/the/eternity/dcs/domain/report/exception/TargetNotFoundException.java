package until.the.eternity.dcs.domain.report.exception;

import static until.the.eternity.dcs.domain.report.exception.ReportExceptionCode.TARGET_NOT_FOUND_EXCEPTION;

import until.the.eternity.dcs.common.exception.CustomException;

public class TargetNotFoundException extends CustomException {
    public TargetNotFoundException(String target) {
        super(
                TARGET_NOT_FOUND_EXCEPTION,
                TARGET_NOT_FOUND_EXCEPTION.getMessage() + " TARGET: " + target);
    }
}
