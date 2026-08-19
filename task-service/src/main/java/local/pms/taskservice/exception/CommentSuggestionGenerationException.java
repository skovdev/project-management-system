package local.pms.taskservice.exception;

/**
 * Thrown when the AI service fails to generate reply suggestions for a comment.
 */
public class CommentSuggestionGenerationException extends RuntimeException {

    public CommentSuggestionGenerationException(String message) {
        super(message);
    }

    public CommentSuggestionGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
