package local.pms.aiservice.service.impl.chatgpt;

import com.openai.client.OpenAIClient;

import com.openai.models.ChatModel;

import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.openai.models.chat.completions.ChatCompletionMessageParam;

import local.pms.aiservice.exception.ChatGptException;

import local.pms.aiservice.service.chatgpt.ChatGptService;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.time.LocalDate;

import java.util.List;
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatGptServiceImpl implements ChatGptService {

    private final OpenAIClient client;

    @Override
    public String askChatGpt(List<ChatCompletionMessageParam> messages) {
        return sendRequestToChatGpt(messages)
                .choices()
                .stream()
                .findFirst()
                .flatMap(choice -> choice.message().content())
                .orElse("No data received from ChatGPT");
    }

    private ChatCompletion sendRequestToChatGpt(List<ChatCompletionMessageParam> messages) {
        log.info("Sending request to ChatGPT - Time: {}", LocalDate.now());
        ChatCompletionCreateParams params = buildChatCompletionParams(messages);
        return executeChatCompletionRequest(params);
    }

    private ChatCompletionCreateParams buildChatCompletionParams(List<ChatCompletionMessageParam> messages) {
        return ChatCompletionCreateParams.builder()
                .model(ChatModel.GPT_4_1_2025_04_14)
                .messages(messages)
                .build();
    }

    private ChatCompletion executeChatCompletionRequest(ChatCompletionCreateParams params) {
        try {
            return client.chat().completions().create(params);
        } catch (Exception e) {
            log.error("Error while communicating with ChatGPT", e);
            throw new ChatGptException("Failed to communicate with ChatGPT");
        }
    }
}
