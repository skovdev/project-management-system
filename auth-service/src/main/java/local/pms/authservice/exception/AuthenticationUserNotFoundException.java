package local.pms.authservice.exception;

public class AuthenticationUserNotFoundException extends RuntimeException {

    public AuthenticationUserNotFoundException(String message) {
        super(message);
    }
}
