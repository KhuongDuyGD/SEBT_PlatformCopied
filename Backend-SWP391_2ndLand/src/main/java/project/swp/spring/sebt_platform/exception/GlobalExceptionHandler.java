package project.swp.spring.sebt_platform.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import project.swp.spring.sebt_platform.dto.response.ErrorResponse;
import project.swp.spring.sebt_platform.validation.FieldErrorDetail;

import java.util.List;
import java.util.UUID;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(ValidationException ex, HttpServletRequest request) {
        String cid = correlationId(request);
        log.warn("[CID={}] Validation failed: {} errors={}", cid, ex.getMessage(), ex.getFieldErrors()!=null?ex.getFieldErrors().size():0);
        ErrorResponse resp = baseError(HttpStatus.BAD_REQUEST, request, "VALIDATION_ERROR", ex.getMessage());
        resp.setFieldErrors(ex.getFieldErrors());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resp);
    }

    @ExceptionHandler({ MultipartException.class, MaxUploadSizeExceededException.class })
    public ResponseEntity<ErrorResponse> handleMultipart(Exception ex, HttpServletRequest request) {
        String cid = correlationId(request);
        log.warn("[CID={}] Multipart upload error: {}", cid, ex.getMessage());
        ErrorResponse resp = baseError(HttpStatus.BAD_REQUEST, request, "UPLOAD_ERROR", "Upload file invalid or too large");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resp);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(MissingServletRequestParameterException ex, HttpServletRequest request) {
        String cid = correlationId(request);
        log.warn("[CID={}] Missing param: {}", cid, ex.getParameterName());
        ErrorResponse resp = baseError(HttpStatus.BAD_REQUEST, request, "MISSING_PARAMETER", "Missing parameter: " + ex.getParameterName());
        resp.setFieldErrors(List.of(new FieldErrorDetail(ex.getParameterName(), "Parameter is required", null)));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resp);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgNotValid(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String cid = correlationId(request);
        BindingResult br = ex.getBindingResult();
        log.warn("[CID={}] Binding validation failed: {} field errors", cid, br.getErrorCount());
        // Nếu có lỗi liên quan images, ghi thêm content-type & query param phục vụ debug
        boolean imageError = br.getFieldErrors().stream().anyMatch(f -> f.getField().startsWith("images"));
        if (imageError) {
            String ct = request.getContentType();
            log.warn("[CID={}] Images binding issue. contentType={} query={}", cid, ct, request.getQueryString());
        }
        ErrorResponse resp = baseError(HttpStatus.BAD_REQUEST, request, "BINDING_ERROR", "Input binding/validation failed");
        List<FieldErrorDetail> fieldErrorDetails = br.getFieldErrors().stream()
                .map(fe -> new FieldErrorDetail(fe.getField(), fe.getDefaultMessage()!=null?fe.getDefaultMessage():"Invalid value", safeRejectedValue(fe)))
                .toList();
        resp.setFieldErrors(fieldErrorDetails);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resp);
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<ErrorResponse> handleInsufficient(InsufficientFundsException ex, HttpServletRequest request) {
        String cid = correlationId(request);
        log.warn("[CID={}] Insufficient funds: required={} current={}", cid, ex.getRequired(), ex.getCurrent());
        ErrorResponse resp = baseError(HttpStatus.CONFLICT, request, "WALLET_INSUFFICIENT_FUNDS", "Insufficient balance for listing fee");
        resp.setRequiredFee(ex.getRequired().toPlainString());
        resp.setCurrentBalance(ex.getCurrent().toPlainString());
        resp.setMessage("Need more funds to pay listing fee");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(resp);
    }

    private Object safeRejectedValue(FieldError fe) {
        Object val = fe.getRejectedValue();
        if (val == null) return null;
        // Avoid logging entire file contents or large objects
        String s = val.toString();
        if (s.length() > 200) {
            return s.substring(0,200) + "...";
        }
        return s;
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
        String cid = correlationId(request);
        log.error("[CID={}] Unhandled exception: {}", cid, ex.getMessage(), ex);
        ErrorResponse resp = baseError(HttpStatus.INTERNAL_SERVER_ERROR, request, "INTERNAL_ERROR", "Internal server error");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
    }

    private ErrorResponse baseError(HttpStatus status, HttpServletRequest req, String code, String message) {
        ErrorResponse resp = new ErrorResponse();
        resp.setStatus(status.value());
        resp.setError(status.getReasonPhrase());
        resp.setPath(req.getRequestURI());
        resp.setCode(code);
        resp.setMessage(message);
        return resp;
    }

    private String correlationId(HttpServletRequest req) {
        Object exist = req.getAttribute("CID");
        if (exist != null) return exist.toString();
        String cid = UUID.randomUUID().toString().substring(0,8);
        req.setAttribute("CID", cid);
        return cid;
    }
}
