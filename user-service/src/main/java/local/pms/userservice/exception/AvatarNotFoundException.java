package local.pms.userservice.exception;

/**
 * Thrown when a delete-avatar operation is attempted but the user has no avatar set.
 */
public class AvatarNotFoundException extends RuntimeException {
    public AvatarNotFoundException(String message) {
        super(message);
    }
}
