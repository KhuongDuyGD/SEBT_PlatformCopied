package project.swp.spring.sebt_platform.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import project.swp.spring.sebt_platform.dto.response.ErrorResponse;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(ValidationException ex, HttpServletRequest request) {
        ErrorResponse resp = new ErrorResponse();
        resp.setPath(request.getRequestURI());
        resp.setStatus(HttpStatus.BAD_REQUEST.value());
        resp.setError(HttpStatus.BAD_REQUEST.getReasonPhrase());
        resp.setMessage(ex.getMessage());
        resp.setCode("VALIDATION_ERROR");
        resp.setFieldErrors(ex.getFieldErrors());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resp);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex);
        ErrorResponse resp = new ErrorResponse();
        resp.setPath(request.getRequestURI());
        resp.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        resp.setError(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
        resp.setMessage("Internal server error");
        resp.setCode("INTERNAL_ERROR");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
    }
}
