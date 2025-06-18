package until.the.eternity.dcs.common.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.METHOD_NOT_ALLOWED;
import static until.the.eternity.dcs.common.exception.GlobalExceptionCode.SERVER_ERROR;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
	@ExceptionHandler(CustomException.class)
	protected ResponseEntity<ExceptionResponse> handleCustomException(CustomException exception) {
		ExceptionResponse response = ExceptionResponse.from(exception);
		return ResponseEntity.status(response.status()).body(response);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ExceptionResponse> handleValidationException(
		MethodArgumentNotValidException ex) {
		String errorMessage =
			ex.getBindingResult().getFieldError() != null
				? ex.getBindingResult().getFieldError().getDefaultMessage()
				: "입력값이 올바르지 않습니다.";
		log.warn("Validation Error: {}", errorMessage);
		return ResponseEntity.status(BAD_REQUEST)
			.body(ExceptionResponse.of(BAD_REQUEST, BAD_REQUEST.name(), errorMessage));
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ExceptionResponse> handleConstraintViolation(
		ConstraintViolationException ex) {
		log.warn("ConstraintViolation: {}", ex.getMessage());
		return ResponseEntity.status(BAD_REQUEST)
			.body(ExceptionResponse.of(BAD_REQUEST, BAD_REQUEST.name(),  ex.getMessage()));
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<ExceptionResponse> handleMethodNotAllowed(
		HttpRequestMethodNotSupportedException ex) {
		log.warn("MethodNotAllowed: {}", ex.getMethod());
		return ResponseEntity.status(METHOD_NOT_ALLOWED)
			.body(ExceptionResponse.of(METHOD_NOT_ALLOWED, METHOD_NOT_ALLOWED.name(), ex.getMessage()));
	}

	@ExceptionHandler(Exception.class)
	protected ResponseEntity<ExceptionResponse> handleException() {
		return ResponseEntity.internalServerError().body(ExceptionResponse.from(SERVER_ERROR));
	}

}
