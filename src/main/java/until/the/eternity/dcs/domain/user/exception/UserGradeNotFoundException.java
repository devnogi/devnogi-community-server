package until.the.eternity.dcs.domain.user.exception;

import until.the.eternity.dcs.common.exception.CustomException;

import static until.the.eternity.dcs.domain.user.exception.UserSummaryExceptionCode.USER_GRADE_NOT_FOUND_EXCEPTION;

public class UserGradeNotFoundException extends CustomException {
    public UserGradeNotFoundException(String grade) {
        super(
                USER_GRADE_NOT_FOUND_EXCEPTION,
                USER_GRADE_NOT_FOUND_EXCEPTION.getMessage() + "grade: " + grade);
    }
}
