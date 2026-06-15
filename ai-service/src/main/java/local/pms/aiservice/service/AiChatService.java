package local.pms.aiservice.service;

/**
 * Provider-agnostic AI chat service.
 * Sends a system prompt and a user prompt to the configured AI model
 * and returns the generated text response.
 */
public interface AiChatService {

    /**
     * Sends the given prompts to the AI model and returns the generated text.
     *
     * @param systemPrompt instruction that sets the model's behaviour and persona
     * @param userPrompt   the user's request or context to act on
     * @return the AI-generated text response
     */
    String chat(String systemPrompt, String userPrompt);
}
