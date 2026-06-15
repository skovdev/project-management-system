package local.pms.aiservice.service;

import local.pms.aiservice.prompt.TaskPrompts;

import local.pms.aiservice.service.impl.TaskAiServiceImpl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;

import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;

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
}
