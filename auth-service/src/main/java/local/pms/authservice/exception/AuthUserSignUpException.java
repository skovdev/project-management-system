package local.pms.authservice.exception;

public class AuthUserSignUpException extends RuntimeException {
    public AuthUserSignUpException(String message) {
        super(message);
    }
}