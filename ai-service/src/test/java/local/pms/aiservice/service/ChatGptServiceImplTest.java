package local.pms.aiservice.service;

import com.openai.client.OpenAIClient;

import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionMessage;
import com.openai.models.chat.completions.ChatCompletionCreateParams;

import local.pms.aiservice.exception.ChatGptException;

import local.pms.aiservice.service.impl.chatgpt.ChatGptServiceImpl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.Answers;
import org.mockito.InjectMocks;

import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class ChatGptServiceImplTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private OpenAIClient client;

    @InjectMocks
    private ChatGptServiceImpl chatGptService;

    @Test
    @DisplayName("askChatGpt returns first choice content when ChatGPT responds")
    void should_returnFirstChoiceContent_when_chatGptResponds() {
        var chatCompletion = mock(ChatCompletion.class);
        var choice = mock(ChatCompletion.Choice.class);
        var message = mock(ChatCompletionMessage.class);

        when(client.chat().completions().create(any(ChatCompletionCreateParams.class))).thenReturn(chatCompletion);
        when(chatCompletion.choices()).thenReturn(List.of(choice));
        when(choice.message()).thenReturn(message);
        when(message.content()).thenReturn(Optional.of("Generated project description"));

        var result = chatGptService.askChatGpt(List.of());

        assertThat(result).isEqualTo("Generated project description");
    }

    @Test
    @DisplayName("askChatGpt returns fallback message when ChatGPT returns no choices")
    void should_returnFallback_when_chatGptHasNoChoices() {
        var chatCompletion = mock(ChatCompletion.class);

        when(client.chat().completions().create(any(ChatCompletionCreateParams.class))).thenReturn(chatCompletion);
        when(chatCompletion.choices()).thenReturn(List.of());

        var result = chatGptService.askChatGpt(List.of());

        assertThat(result).isEqualTo("No data received from ChatGPT");
    }

    @Test
    @DisplayName("askChatGpt throws ChatGptException when OpenAI client fails")
    void should_throwChatGptException_when_clientFails() {
        when(client.chat().completions().create(any(ChatCompletionCreateParams.class)))
                .thenThrow(new RuntimeException("Network error"));

        assertThatThrownBy(() -> chatGptService.askChatGpt(List.of()))
                .isInstanceOf(ChatGptException.class)
                .hasMessageContaining("Failed to communicate with ChatGPT");
    }
}
