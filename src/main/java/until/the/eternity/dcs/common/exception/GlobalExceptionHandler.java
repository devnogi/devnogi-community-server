package until.the.eternity.dcs.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import until.the.eternity.dcs.common.response.ApiResponse;
import until.the.eternity.dcs.domain.announcement.exception.AnnouncementInvalidBoardIdParameterException;

import static until.the.eternity.dcs.common.exception.GlobalExceptionCode.SERVER_ERROR;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(CustomException.class)
    protected ResponseEntity<ApiResponse<?>> handleCustomException(CustomException exception) {
        ExceptionResponse exResponse = ExceptionResponse.from(exception);
        ApiResponse<?> response = ApiResponse.error(exResponse.code(), exResponse.message());
        return ResponseEntity.status(exResponse.status()).body(response);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ResponseEntity<ApiResponse<?>> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException exception) {
        if ("boardId".equals(exception.getName()) && exception.getRequiredType() == Long.class) {
            return handleCustomException(new AnnouncementInvalidBoardIdParameterException());
        }
        return handleException(exception);
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ApiResponse<?>> handleException(Exception exception) {
        ExceptionResponse exResponse = ExceptionResponse.from(SERVER_ERROR);
        ApiResponse<?> response = ApiResponse.error(exResponse.code(), exResponse.message());
        return ResponseEntity.status(exResponse.status()).body(response);
    }
}
