package until.the.eternity.dcs.domain.post.exception;

import static until.the.eternity.dcs.domain.post.exception.PostExceptionCode.POST_IMAGE_COUNT_EXCEEDED_EXCEPTION;

import until.the.eternity.dcs.common.exception.CustomException;

public class PostImageCountExceededException extends CustomException {
    public PostImageCountExceededException() {
        super(POST_IMAGE_COUNT_EXCEEDED_EXCEPTION);
    }
}
