package project.swp.spring.sebt_platform.dto.response;

public class SuccessResponseDTO {
    private String message;
    private boolean success = true;
    private Object data;

    // Constructors
    public SuccessResponseDTO(String message) {
        this.message = message;
    }

    public SuccessResponseDTO(String message, Object data) {
        this.message = message;
        this.data = data;
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

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
