package local.pms.taskservice.exception;

/**
 * Thrown when the AI service fails to generate acceptance criteria for a task.
 */
public class AcceptanceCriteriaGenerationException extends RuntimeException {

    public AcceptanceCriteriaGenerationException(String message) {
        super(message);
    }

    public AcceptanceCriteriaGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
