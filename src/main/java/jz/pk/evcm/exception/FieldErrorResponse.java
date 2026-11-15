package jz.pk.evcm.exception;

import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

public record FieldErrorResponse(String field, String message) {

    static FieldErrorResponse from(ObjectError error) {
        String field = error instanceof FieldError fieldError ? fieldError.getField() : error.getObjectName();
        return new FieldErrorResponse(field, error.getDefaultMessage());
    }
}
