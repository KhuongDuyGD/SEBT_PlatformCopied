package project.swp.spring.sebt_platform.exception;

public class AuthRequiredException extends RuntimeException {
    public AuthRequiredException() { super("login required"); }
    public AuthRequiredException(String message) { super(message); }
}
