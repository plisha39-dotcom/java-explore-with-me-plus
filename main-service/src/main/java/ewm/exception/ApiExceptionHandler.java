package ewm.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class ApiExceptionHandler {
    private static final String NOT_FOUND_REASON = "The required object was not found.";
    private static final String BAD_REQUEST_REASON = "Incorrectly made request.";

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(NotFoundException exception) {
        return response(HttpStatus.NOT_FOUND, NOT_FOUND_REASON, exception.getMessage(), List.of());
    }

    @ExceptionHandler(BadRequestException.class)
    private ResponseEntity<ApiError> handleBadRequest(BadRequestException exception) {
        return response(HttpStatus.BAD_REQUEST, BAD_REQUEST_REASON, exception.getMessage(), List.of());
    }

    private ResponseEntity<ApiError> response(HttpStatus status, String reason,
                                              String message, List<String> errors) {
        ApiError body = new ApiError(errors, message, reason, status.name(), LocalDateTime.now());
        return ResponseEntity.status(status).body(body);
    }
}
