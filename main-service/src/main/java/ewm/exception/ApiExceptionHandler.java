package ewm.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.validation.FieldError;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class ApiExceptionHandler {
    private static final String NOT_FOUND_REASON = "The required object was not found.";
    private static final String CONFLICT_REASON = "For the requested operation the conditions are not met.";
    private static final String BAD_REQUEST_REASON = "Incorrectly made request.";

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(NotFoundException exception) {
        return response(HttpStatus.NOT_FOUND, NOT_FOUND_REASON, exception.getMessage(), List.of());
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> handleConflict(ConflictException exception) {
        return response(HttpStatus.CONFLICT, CONFLICT_REASON, exception.getMessage(), List.of());
    }

    @ExceptionHandler(BadRequestException.class)
    private ResponseEntity<ApiError> handleBadRequest(BadRequestException exception) {
        return response(HttpStatus.BAD_REQUEST, BAD_REQUEST_REASON, exception.getMessage(), List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    private ResponseEntity<ApiError> handleArgumentNotValid(MethodArgumentNotValidException exception) {
        List<FieldError> fieldErrors = exception.getBindingResult().getFieldErrors();
        List<String> errors = fieldErrors.stream()
                                         .map(error -> "Field: " + error.getField() + ". Error: " + error.getDefaultMessage())
                                         .toList();
        String message = errors.stream().findFirst().orElse(BAD_REQUEST_REASON);
        return response(HttpStatus.BAD_REQUEST, BAD_REQUEST_REASON, message, errors);
    }

    private ResponseEntity<ApiError> response(HttpStatus status, String reason,
                                              String message, List<String> errors) {
        ApiError body = new ApiError(errors, message, reason, status.name(), LocalDateTime.now());
        return ResponseEntity.status(status).body(body);
    }
}
