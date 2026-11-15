package jz.pk.evcm.exception;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.core.JacksonException;

import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Object> handleResponseStatusException(ResponseStatusException ex) {
        return ResponseEntity.status(ex.getStatusCode()).body(
                Map.of("message", ex.getReason())
        );
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Object> handleAuthenticationException(AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                Map.of("error", "Authentication failed", "message", ex.getMessage())
        );
    }

    @ExceptionHandler(ForbiddenAccessException.class)
    public ResponseEntity<Object> handleForbiddenAccessException(ForbiddenAccessException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                Map.of("error", "Access Denied", "message", ex.getMessage())
        );
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Object> handleEntityNotFoundException(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                Map.of("error", "Not Found", "message", "Resource not found")
        );
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public  ResponseEntity<Object> handleEmailAlreadyExistsException(EmailAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                Map.of("error", "Conflict", "message", ex.getMessage())
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        List<FieldErrorResponse> errors = ex.getBindingResult().getAllErrors().stream()
                .map(FieldErrorResponse::from)
                .toList();
        return validationFailed(errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        String field = ex.getMostSpecificCause() instanceof JacksonException jacksonEx
                ? fieldPath(jacksonEx.getPath())
                : "";
        if (field.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Bad Request", "message", "Malformed request body")
            );
        }
        return validationFailed(List.of(new FieldErrorResponse(field, "Invalid value")));
    }

    private static ResponseEntity<Object> validationFailed(List<FieldErrorResponse> errors) {
        return ResponseEntity.badRequest().body(
                Map.of("error", "Bad Request", "message", "Validation failed", "errors", errors)
        );
    }

    private static String fieldPath(List<JacksonException.Reference> path) {
        StringBuilder sb = new StringBuilder();
        for (JacksonException.Reference ref : path) {
            if (ref.getPropertyName() != null) {
                if (!sb.isEmpty()) {
                    sb.append('.');
                }
                sb.append(ref.getPropertyName());
            } else if (ref.getIndex() >= 0) {
                sb.append('[').append(ref.getIndex()).append(']');
            }
        }
        return sb.toString();
    }
}
