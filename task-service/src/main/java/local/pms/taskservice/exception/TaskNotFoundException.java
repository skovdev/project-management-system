package local.pms.taskservice.exception;

/**
 * Exception thrown when a requested task does not exist in the system.
 */
public class TaskNotFoundException extends RuntimeException {

    public TaskNotFoundException(String message) {
        super(message);
    }
}
