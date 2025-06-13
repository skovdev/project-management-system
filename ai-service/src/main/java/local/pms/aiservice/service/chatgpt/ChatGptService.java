package local.pms.aiservice.service.chatgpt;

import local.pms.aiservice.model.Message;

import java.util.List;

/**
 * Interface representing the ChatGPT service.
 * <p>
 * This service is responsible for interacting with ChatGPT to send messages
 * and retrieve responses. Implementations of this interface should define
 * the logic for communicating with the ChatGPT API.
 * </p>
 */
public interface ChatGptService {

    /**
     * Sends a list of messages to ChatGPT and retrieves the response.
     * <p>
     * This method builds a ChatRequest object using the provided messages, sends the request
     * to the ChatGPT service, and processes the response to extract the content of the first choice.
     * If no response is received or the choices list is empty, a default message is returned.
     * </p>
     * @param messages A list of Message objects representing the conversation history.
     * @return The content of the first choice from the ChatGPT response, or "No response from ChatGPT" if no response is available.
     */
    String askChatGpt(List<Message> messages);

}
