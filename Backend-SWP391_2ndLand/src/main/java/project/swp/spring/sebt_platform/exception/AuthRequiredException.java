package project.swp.spring.sebt_platform.exception;

public class AuthRequiredException extends RuntimeException {
    public AuthRequiredException() { super("login required"); }
}
