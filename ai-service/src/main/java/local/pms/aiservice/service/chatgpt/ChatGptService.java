package local.pms.aiservice.service.chatgpt;

import com.openai.models.chat.completions.ChatCompletionMessageParam;

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
     * Sends a list of messages to the ChatGPT API and retrieves the response.
     *
     * @param messages A list of message parameters to be sent to ChatGPT.
     *                 Each message contains the role and content for the conversation.
     * @return The response from ChatGPT as a string.
     * */
    String askChatGpt(List<ChatCompletionMessageParam> messages);

}
