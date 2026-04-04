package local.pms.taskservice.exception;

/**
 * Exception thrown when an authenticated user attempts to access or modify
 * a task they do not own.
 */
public class TaskAccessDeniedException extends RuntimeException {

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message the detail message
     */
    public TaskAccessDeniedException(String message) {
        super(message);
    }
}
