package project.swp.spring.sebt_platform.dto.response;

public class ErrorResponseDTO {
    private String message;
    private boolean success = false;
    private String errorCode;

    // Constructors
    public ErrorResponseDTO(String message) {
        this.message = message;
    }

    public ErrorResponseDTO(String message, String errorCode) {
        this.message = message;
        this.errorCode = errorCode;
    }

    // Getters and setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getErrorCode() {
        return errorCode;
    }
}

