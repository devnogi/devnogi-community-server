package until.the.eternity.dcs.domain.post.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import until.the.eternity.dcs.common.exception.ExceptionCode;

@Getter
@RequiredArgsConstructor
public enum PostContractExceptionCode implements ExceptionCode {
    INVALID_PLAIN_CONTENT(HttpStatus.BAD_REQUEST, "본문은 1자 이상 1,000자 이하여야 합니다."),
    INVALID_MEDIA(HttpStatus.BAD_REQUEST, "지원하지 않거나 손상된 미디어 파일입니다."),
    MEDIA_TOO_LARGE(HttpStatus.PAYLOAD_TOO_LARGE, "미디어 파일은 각각 5MiB 이하여야 합니다."),
    MEDIA_COUNT_EXCEEDED(HttpStatus.BAD_REQUEST, "미디어는 최대 5개, 영상은 최대 1개까지 첨부할 수 있습니다.");

    private final HttpStatus status;
    private final String message;

    @Override
    public String getCode() {
        return name();
    }
}
