package local.pms.aiservice.service;

import local.pms.aiservice.exception.AiChatException;

import local.pms.aiservice.service.impl.AiChatServiceImpl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.Answers;
import org.mockito.InjectMocks;

import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.ai.chat.client.ChatClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiChatServiceImplTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ChatClient chatClient;

    @InjectMocks
    private AiChatServiceImpl aiChatService;

    @Test
    @DisplayName("chat returns content when AI model responds")
    void should_returnContent_when_aiModelResponds() {
        when(chatClient.prompt()
                .system(any(String.class))
                .user(any(String.class))
                .call()
                .content())
                .thenReturn("Generated acceptance criteria");

        String result = aiChatService.chat("You are a helpful assistant.", "Generate criteria.");

        assertThat(result).isEqualTo("Generated acceptance criteria");
    }

    @Test
    @DisplayName("chat returns fallback when AI model returns null content")
    void should_returnFallback_when_aiModelReturnsNull() {
        when(chatClient.prompt()
                .system(any(String.class))
                .user(any(String.class))
                .call()
                .content())
                .thenReturn(null);

        String result = aiChatService.chat("You are a helpful assistant.", "Generate criteria.");

        assertThat(result).isEqualTo("No response received from AI model");
    }

    @Test
    @DisplayName("chat throws AiChatException when AI model call fails")
    void should_throwAiChatException_when_modelCallFails() {
        when(chatClient.prompt()
                .system(any(String.class))
                .user(any(String.class))
                .call()
                .content())
                .thenThrow(new RuntimeException("Connection refused"));

        assertThatThrownBy(() -> aiChatService.chat("You are a helpful assistant.", "Generate criteria."))
                .isInstanceOf(AiChatException.class)
                .hasMessageContaining("Failed to communicate with AI model");
    }
}
