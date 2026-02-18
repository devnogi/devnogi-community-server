package until.the.eternity.dcs.domain.user.exception;

import static until.the.eternity.dcs.domain.user.exception.UserSummaryExceptionCode.USER_ALREADY_EXISTS_EXCEPTION;

import until.the.eternity.dcs.common.exception.CustomException;

public class UserAlreadyExistsException extends CustomException {
    public UserAlreadyExistsException(Long userId) {
        super(
                USER_ALREADY_EXISTS_EXCEPTION,
                USER_ALREADY_EXISTS_EXCEPTION.getMessage() + " userId : " + userId);
    }
}
