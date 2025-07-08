package until.the.eternity.dcs.domain.comment.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import until.the.eternity.dcs.common.exception.ExceptionCode;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Getter
@RequiredArgsConstructor
public enum CommentExceptionCode implements ExceptionCode {
	COMMENT_NOT_FOUND_EXCEPTION(NOT_FOUND, "해당 댓글 찾을 수 없습니다."),
	COMMENT_MODIFY_FORBIDDEN_EXCEPTION(FORBIDDEN, "글쓴이만 댓글을 편집할 수 있습니다."),
	;

	private final HttpStatus status;
	private final String message;

	@Override
	public String getCode() {
		return this.name();
	}
}
