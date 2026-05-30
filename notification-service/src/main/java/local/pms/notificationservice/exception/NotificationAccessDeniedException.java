package local.pms.notificationservice.exception;

/**
 * Thrown when a user attempts to access a notification that belongs to another user.
 */
public class NotificationAccessDeniedException extends RuntimeException {

    public NotificationAccessDeniedException(String message) {
        super(message);
    }
}
