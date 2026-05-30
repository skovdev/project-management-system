package local.pms.notificationservice.exception;

/**
 * Thrown when a requested notification does not exist or is soft-deleted.
 */
public class NotificationNotFoundException extends RuntimeException {

    public NotificationNotFoundException(String message) {
        super(message);
    }
}
