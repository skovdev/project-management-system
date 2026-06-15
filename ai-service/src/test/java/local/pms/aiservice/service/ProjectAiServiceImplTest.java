package local.pms.aiservice.service;

import local.pms.aiservice.prompt.ProjectPrompts;

import local.pms.aiservice.service.impl.ProjectAiServiceImpl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.InjectMocks;

import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.ArgumentMatchers.eq;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProjectAiServiceImplTest {

    @Mock
    private AiChatService aiChatService;

    @InjectMocks
    private ProjectAiServiceImpl projectAiService;

    @Test
    @DisplayName("generateProjectDescription returns AI response when chat succeeds")
    void should_returnDescription_when_chatSucceeds() {
        var expectedUserPrompt = "Generate a project description for the following title: PMS";
        when(aiChatService.chat(ProjectPrompts.SYSTEM_PROMPT_PROJECT_DESCRIPTION, expectedUserPrompt))
                .thenReturn("A comprehensive project management system.");

        var result = projectAiService.generateProjectDescription("PMS");

        assertThat(result).isEqualTo("A comprehensive project management system.");
        verify(aiChatService).chat(eq(ProjectPrompts.SYSTEM_PROMPT_PROJECT_DESCRIPTION), eq(expectedUserPrompt));
    }

    @Test
    @DisplayName("generateProjectDescription composes user prompt from title")
    void should_composeUserPrompt_with_title() {
        var title = "HR Portal";
        var expectedPrompt = "Generate a project description for the following title: " + title;
        when(aiChatService.chat(eq(ProjectPrompts.SYSTEM_PROMPT_PROJECT_DESCRIPTION), eq(expectedPrompt)))
                .thenReturn("An HR management portal.");

        var result = projectAiService.generateProjectDescription(title);

        assertThat(result).isEqualTo("An HR management portal.");
    }

    @Test
    @DisplayName("generateProjectDescription propagates exception from AiChatService")
    void should_propagateException_when_chatFails() {
        when(aiChatService.chat(eq(ProjectPrompts.SYSTEM_PROMPT_PROJECT_DESCRIPTION),
                eq("Generate a project description for the following title: X")))
                .thenThrow(new RuntimeException("AI model unavailable"));

        assertThatThrownBy(() -> projectAiService.generateProjectDescription("X"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("AI model unavailable");
    }
}
