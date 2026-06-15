package local.pms.aiservice.exception;

/**
 * Thrown when communication with the AI model fails.
 */
public class AiChatException extends RuntimeException {

    public AiChatException(String message) {
        super(message);
    }
}
