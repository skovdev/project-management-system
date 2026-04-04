package local.pms.userservice.exception;

/**
 * Thrown when an authenticated user attempts to access or modify
 * a user resource they do not own and do not have admin privileges for.
 */
public class UserAccessDeniedException extends RuntimeException {

    public UserAccessDeniedException(String message) {
        super(message);
    }
}
