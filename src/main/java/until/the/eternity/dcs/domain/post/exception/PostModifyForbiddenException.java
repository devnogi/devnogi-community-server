package until.the.eternity.dcs.domain.post.exception;

import until.the.eternity.dcs.common.exception.CustomException;

import static until.the.eternity.dcs.domain.post.exception.PostExceptionCode.POST_MODIFY_FORBIDDEN_EXCEPTION;

public class PostModifyForbiddenException extends CustomException {
  public PostModifyForbiddenException()  {
      super(POST_MODIFY_FORBIDDEN_EXCEPTION);
  }
}
