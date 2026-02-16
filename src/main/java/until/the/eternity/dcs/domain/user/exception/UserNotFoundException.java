package until.the.eternity.dcs.domain.user.exception;

import until.the.eternity.dcs.common.exception.CustomException;

import static until.the.eternity.dcs.domain.user.exception.UserSummaryExceptionCode.USER_NOT_FOUND_EXCEPTION;

public class UserNotFoundException extends CustomException {
    public UserNotFoundException(Long userId) {
        super(
                USER_NOT_FOUND_EXCEPTION,
                USER_NOT_FOUND_EXCEPTION.getMessage() + " userId : " + userId);
    }

    public UserNotFoundException() {
        super(USER_NOT_FOUND_EXCEPTION, USER_NOT_FOUND_EXCEPTION.getMessage());
    }
}
