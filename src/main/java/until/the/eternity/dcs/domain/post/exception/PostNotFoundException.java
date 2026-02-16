package until.the.eternity.dcs.domain.post.exception;

import until.the.eternity.dcs.common.exception.CustomException;

import static until.the.eternity.dcs.domain.post.exception.PostExceptionCode.POST_NOT_FOUND_EXCEPTION;

public class PostNotFoundException extends CustomException {
    public PostNotFoundException(Long id) {
        super(POST_NOT_FOUND_EXCEPTION, POST_NOT_FOUND_EXCEPTION.getMessage() + " ID : " + id);
    }
}
