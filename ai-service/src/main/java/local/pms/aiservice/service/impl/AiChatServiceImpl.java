package local.pms.aiservice.service.impl;

import local.pms.aiservice.exception.AiChatException;

import local.pms.aiservice.service.AiChatService;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.ai.chat.client.ChatClient;

import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * Spring AI–backed implementation of {@link AiChatService}.
 * Delegates to {@link ChatClient} which is wired to the OpenAI model
 * configured via {@code spring.ai.openai.chat.options.model}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiChatServiceImpl implements AiChatService {

    private static final String NO_RESPONSE_FALLBACK = "No response received from AI model";

    private final ChatClient chatClient;

    /**
     * {@inheritDoc}
     *
     * @throws AiChatException if the underlying AI model call fails
     */
    @Override
    public String chat(String systemPrompt, String userPrompt) {
        log.info("Sending request to AI model - Date: {}", LocalDate.now());
        try {
            var content = chatClient.prompt()
                    .system(systemPrompt)
                    .user(userPrompt)
                    .call()
                    .content();
            return content != null ? content : NO_RESPONSE_FALLBACK;
        } catch (Exception e) {
            log.error("Error while communicating with AI model", e);
            throw new AiChatException("Failed to communicate with AI model");
        }
    }
}
