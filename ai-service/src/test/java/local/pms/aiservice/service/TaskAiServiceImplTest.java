package local.pms.aiservice.service;

import local.pms.aiservice.prompt.TaskPrompts;

import local.pms.aiservice.service.impl.TaskAiServiceImpl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;

import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.ArgumentMatchers.eq;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TaskAiServiceImplTest {

    @Mock
    private AiChatService aiChatService;

    @InjectMocks
    private TaskAiServiceImpl taskAiService;

    @Test
    @DisplayName("generateAcceptanceCriteria returns AI response when chat succeeds")
    void should_returnCriteria_when_chatSucceeds() {
        var expectedUserPrompt = "Task title: Login feature\nTask description: Allow users to log in";
        when(aiChatService.chat(TaskPrompts.SYSTEM_PROMPT_ACCEPTANCE_CRITERIA, expectedUserPrompt))
                .thenReturn("Given the user is on the login page...");

        var result = taskAiService.generateAcceptanceCriteria("Login feature", "Allow users to log in");

        assertThat(result).isEqualTo("Given the user is on the login page...");
        verify(aiChatService).chat(eq(TaskPrompts.SYSTEM_PROMPT_ACCEPTANCE_CRITERIA), eq(expectedUserPrompt));
    }

    @Test
    @DisplayName("generateAcceptanceCriteria composes user prompt from title and description")
    void should_composeUserPrompt_with_titleAndDescription() {
        when(aiChatService.chat(
                eq(TaskPrompts.SYSTEM_PROMPT_ACCEPTANCE_CRITERIA),
                eq("Task title: Upload file\nTask description: User can upload a CSV")))
                .thenReturn("1. Given...");

        var result = taskAiService.generateAcceptanceCriteria("Upload file", "User can upload a CSV");

        assertThat(result).isEqualTo("1. Given...");
    }

    @Test
    @DisplayName("generateAcceptanceCriteria propagates exception from AiChatService")
    void should_propagateException_when_chatFails() {
        when(aiChatService.chat(eq(TaskPrompts.SYSTEM_PROMPT_ACCEPTANCE_CRITERIA), eq("Task title: X\nTask description: Y")))
                .thenThrow(new RuntimeException("AI model unavailable"));

        org.assertj.core.api.Assertions.assertThatThrownBy(
                () -> taskAiService.generateAcceptanceCriteria("X", "Y"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("AI model unavailable");
    }

    @Test
    @DisplayName("generateCommentSuggestions returns 3 parsed suggestions when chat succeeds without thread context")
    void should_returnParsedSuggestions_when_chatSucceedsWithoutThreadContext() {
        var expectedUserPrompt = "Task title: Login feature\n" +
                "Task description: Allow users to log in\n" +
                "Thread context: none\n" +
                "Comment to reply to: Should we support SSO too?";
        when(aiChatService.chat(eq(TaskPrompts.SYSTEM_PROMPT_COMMENT_SUGGESTIONS), eq(expectedUserPrompt)))
                .thenReturn("1. Great idea, let's add SSO support.\n2. Not for this release, but let's track it.\n3. Can you open a follow-up ticket for SSO?");

        var result = taskAiService.generateCommentSuggestions(
                "Login feature", "Allow users to log in", "Should we support SSO too?", null);

        assertThat(result).containsExactly(
                "Great idea, let's add SSO support.",
                "Not for this release, but let's track it.",
                "Can you open a follow-up ticket for SSO?");
        verify(aiChatService).chat(eq(TaskPrompts.SYSTEM_PROMPT_COMMENT_SUGGESTIONS), eq(expectedUserPrompt));
    }

    @Test
    @DisplayName("generateCommentSuggestions composes user prompt with numbered thread context when provided")
    void should_composeUserPrompt_with_threadContext() {
        var expectedUserPrompt = "Task title: Upload file\n" +
                "Task description: User can upload a CSV\n" +
                "Thread context (previous comments, most recent first):\n" +
                "1. Looks good to me.\n" +
                "2. What about file size limits?\n" +
                "Comment to reply to: Max 10MB should be enough.";
        when(aiChatService.chat(eq(TaskPrompts.SYSTEM_PROMPT_COMMENT_SUGGESTIONS), eq(expectedUserPrompt)))
                .thenReturn("1. Agreed, 10MB sounds reasonable.\n2. Should we make the limit configurable?\n3. Let's document that limit in the API spec.");

        var result = taskAiService.generateCommentSuggestions(
                "Upload file", "User can upload a CSV", "Max 10MB should be enough.",
                List.of("Looks good to me.", "What about file size limits?"));

        assertThat(result).hasSize(3);
        verify(aiChatService).chat(eq(TaskPrompts.SYSTEM_PROMPT_COMMENT_SUGGESTIONS), eq(expectedUserPrompt));
    }

    @Test
    @DisplayName("generateCommentSuggestions strips numbering variants and ignores blank lines")
    void should_stripNumberingAndBlankLines_when_parsingResponse() {
        when(aiChatService.chat(eq(TaskPrompts.SYSTEM_PROMPT_COMMENT_SUGGESTIONS), org.mockito.ArgumentMatchers.any()))
                .thenReturn("1. First suggestion.\n\n2) Second suggestion.\n3.Third suggestion.\n");

        var result = taskAiService.generateCommentSuggestions("Title", "Description", "Comment", List.of());

        assertThat(result).containsExactly("First suggestion.", "Second suggestion.", "Third suggestion.");
    }

    @Test
    @DisplayName("generateCommentSuggestions propagates exception from AiChatService")
    void should_propagateException_when_commentSuggestionsChatFails() {
        when(aiChatService.chat(eq(TaskPrompts.SYSTEM_PROMPT_COMMENT_SUGGESTIONS), org.mockito.ArgumentMatchers.any()))
                .thenThrow(new RuntimeException("AI model unavailable"));

        org.assertj.core.api.Assertions.assertThatThrownBy(
                () -> taskAiService.generateCommentSuggestions("Title", "Description", "Comment", null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("AI model unavailable");
    }
}
