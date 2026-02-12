package until.the.eternity.dcs.domain.post.exception;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import until.the.eternity.dcs.common.exception.ExceptionCode;

@Getter
@RequiredArgsConstructor
public enum PostExceptionCode implements ExceptionCode {
    POST_MODIFY_FORBIDDEN_EXCEPTION(FORBIDDEN, "자신의 게시글만 수정 할 수 있습니다."),
    POST_NOT_FOUND_EXCEPTION(NOT_FOUND, "해당 게시글을 찾을 수 없습니다."),
    POST_DELETION_NOT_ALLOWED_EXCEPTION(FORBIDDEN, "자신의 게시글만 삭제할 수 있습니다."),
    POST_DRAFT_REQUIRED_EXCEPTION(BAD_REQUEST, "게시글 임시저장 여부(isDraft)는 필수입니다.");

    private final HttpStatus status;
    private final String message;

    @Override
    public String getCode() {
        return this.name();
    }
}
