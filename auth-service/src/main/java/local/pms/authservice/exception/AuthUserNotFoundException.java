package local.pms.authservice.exception;

public class AuthUserNotFoundException extends RuntimeException {

    public AuthUserNotFoundException(String message) {
        super(message);
    }
}
