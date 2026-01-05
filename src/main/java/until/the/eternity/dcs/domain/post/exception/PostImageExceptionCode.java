package until.the.eternity.dcs.domain.post.exception;

import static org.springframework.http.HttpStatus.*;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import until.the.eternity.dcs.common.exception.ExceptionCode;

@Getter
@RequiredArgsConstructor
public enum PostImageExceptionCode implements ExceptionCode {
    MISSING_FILE_UPLOAD_EXCEPTION(NOT_FOUND, "업로드 할 이미지를 찾을 수 없습니다."),
    INVALID_EXTENSION_EXCEPTION(
            BAD_REQUEST, "지원하지 않는 파일 형식입니다. 이미지 파일(.jpg, .png, .gif)만 업로드 가능합니다."),
    INVALID_FILE_NAME_EXCEPTION(BAD_REQUEST, "파일 이름이 유효하지 않습니다. 확장자를 포함해주세요.");

    private final HttpStatus status;
    private final String message;

    @Override
    public String getCode() {
        return this.name();
    }
}
