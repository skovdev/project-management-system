package local.pms.projectservice.exception;

/**
 * Exception thrown when an authenticated user attempts to access or modify
 * a project they do not own.
 */
public class ProjectAccessDeniedException extends RuntimeException {

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message the detail message
     */
    public ProjectAccessDeniedException(String message) {
        super(message);
    }
}
