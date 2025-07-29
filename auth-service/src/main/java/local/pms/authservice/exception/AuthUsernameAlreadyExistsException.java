package local.pms.authservice.exception;

public class AuthUsernameAlreadyExistsException extends RuntimeException {

    public AuthUsernameAlreadyExistsException(String message) {
        super(message);
    }
}
