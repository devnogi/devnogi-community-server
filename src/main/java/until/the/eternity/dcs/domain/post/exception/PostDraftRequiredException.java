package until.the.eternity.dcs.domain.post.exception;

import static until.the.eternity.dcs.domain.post.exception.PostExceptionCode.POST_DRAFT_REQUIRED_EXCEPTION;

import until.the.eternity.dcs.common.exception.CustomException;

public class PostDraftRequiredException extends CustomException {
    public PostDraftRequiredException() {
        super(POST_DRAFT_REQUIRED_EXCEPTION);
    }
}
