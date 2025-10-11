package project.swp.spring.sebt_platform.exception;

import project.swp.spring.sebt_platform.validation.FieldErrorDetail;

import java.util.List;

/**
 * Thrown when manual validation on incoming request fails.
 */
public class ValidationException extends RuntimeException {
    private final List<FieldErrorDetail> fieldErrors;

    public ValidationException(String message, List<FieldErrorDetail> fieldErrors) {
        super(message);
        this.fieldErrors = fieldErrors == null ? List.of() : List.copyOf(fieldErrors);
    }

    public List<FieldErrorDetail> getFieldErrors() {
        return fieldErrors;
    }
}
