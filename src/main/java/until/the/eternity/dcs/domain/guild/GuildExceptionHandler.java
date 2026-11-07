package until.the.eternity.dcs.domain.guild;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import until.the.eternity.dcs.common.response.ApiResponse;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(basePackageClasses = GuildAccess.class)
public class GuildExceptionHandler {
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponse<Void>> status(ResponseStatusException ex) {
        return ResponseEntity.status(ex.getStatusCode())
                .body(ApiResponse.error("GUILD_" + ex.getStatusCode().value(), ex.getReason()));
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<ApiResponse<Void>> conflict(DuplicateKeyException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error("GUILD_409", "이미 등록되었거나 처리된 요청입니다."));
    }
}
