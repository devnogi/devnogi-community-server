package until.the.eternity.dcs.domain.post.exception;

import until.the.eternity.dcs.common.exception.CustomException;

import static until.the.eternity.dcs.domain.post.exception.PostExceptionCode.POST_IMAGE_COUNT_EXCEEDED_EXCEPTION;

public class PostImageCountExceededException extends CustomException {
    public PostImageCountExceededException() {
        super(POST_IMAGE_COUNT_EXCEEDED_EXCEPTION);
    }
}
