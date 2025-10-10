package project.swp.spring.sebt_platform.dto.response;

import project.swp.spring.sebt_platform.validation.FieldErrorDetail;

import java.time.Instant;
import java.util.List;

public class ErrorResponse {
    private Instant timestamp = Instant.now();
    private String path;
    private int status;
    private String error;
    private String message;
    private String code; // optional application code
    private List<FieldErrorDetail> fieldErrors;

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public List<FieldErrorDetail> getFieldErrors() { return fieldErrors; }
    public void setFieldErrors(List<FieldErrorDetail> fieldErrors) { this.fieldErrors = fieldErrors; }
}
