package project.swp.spring.sebt_platform.exception;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) { super(message); }
    public NotFoundException(String resource, Object id) { super(resource + " not found: " + id); }
}
