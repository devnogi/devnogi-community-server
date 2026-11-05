package until.the.eternity.dcs.domain.post.exception;

import until.the.eternity.dcs.common.exception.CustomException;

public class PostContractException extends CustomException {
    public PostContractException(PostContractExceptionCode code) {
        super(code);
    }
}
