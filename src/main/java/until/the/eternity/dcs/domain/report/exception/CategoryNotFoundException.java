package until.the.eternity.dcs.domain.report.exception;

import until.the.eternity.dcs.common.exception.CustomException;

import static until.the.eternity.dcs.domain.report.exception.ReportExceptionCode.CATEGORY_NOT_FOUND_EXCEPTION;

public class CategoryNotFoundException extends CustomException {
    public CategoryNotFoundException(String category) {
        super(
                CATEGORY_NOT_FOUND_EXCEPTION,
                CATEGORY_NOT_FOUND_EXCEPTION.getMessage() + " CATEGORY: " + category);
    }
}
