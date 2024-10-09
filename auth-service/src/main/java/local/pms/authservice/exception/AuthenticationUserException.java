package local.pms.authservice.exception;

import org.springframework.security.core.AuthenticationException;

public class AuthenticationUserException extends AuthenticationException {

    public AuthenticationUserException(String msg) {
        super(msg);
    }
}