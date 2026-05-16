package local.pms.projectservice.external.ai.provider;

import com.openai.models.chat.completions.ChatCompletionMessageParam;
import com.openai.models.chat.completions.ChatCompletionUserMessageParam;
import com.openai.models.chat.completions.ChatCompletionSystemMessageParam;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

import io.github.resilience4j.retry.annotation.Retry;

import local.pms.projectservice.dto.api.response.ApiResponseDto;

import local.pms.projectservice.exception.DescriptionGenerationException;

import local.pms.projectservice.external.ai.client.AiFeignClient;
import local.pms.projectservice.external.ai.client.AiChatRequestDto;

import local.pms.projectservice.external.ai.client.promt.PromptMessage;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenAiExternalProvider implements AiExternalProvider {

    private static final String FALLBACK_MESSAGE_TEMPLATE = "Project description generation is temporarily unavailable. Reference ID: %s";

    private final AiFeignClient aiFeignClient;

    @Override
    @CircuitBreaker(name = "projectDescriptionAiGeneration", fallbackMethod = "fallbackProjectDescription")
    @Retry(name = "projectDescriptionAiGeneration", fallbackMethod = "fallbackProjectDescription")
    public String generateProjectDescription(String projectTitle) {
        ApiResponseDto<String> response = aiFeignClient.generateProjectDescription(new AiChatRequestDto(fillChatGptMessages(projectTitle)));
        if (response == null) {
            log.warn("AI service returned a null response for projectTitle='{}'", projectTitle);
            throw new DescriptionGenerationException(
                    "AI service returned a null response for projectTitle='" + projectTitle + "'"
            );
        }
        if (response.getErrors() != null && !response.getErrors().isEmpty()) {
            log.warn("AI service returned errors for projectTitle='{}': {}", projectTitle, response.getErrors());
            throw new DescriptionGenerationException(
                    "AI service returned errors for projectTitle='" + projectTitle + "': " + response.getErrors()
            );
        }
        String description = response.getData();
        if (description == null || description.isBlank()) {
            log.warn("AI service returned an empty description for projectTitle='{}'", projectTitle);
            throw new DescriptionGenerationException(
                    "AI service returned an empty description for projectTitle='" + projectTitle + "'"
            );
        }
        return description;
    }

    private List<ChatCompletionMessageParam> fillChatGptMessages(String projectTitle) {
        return List.of(
                ChatCompletionMessageParam.ofSystem(
                        ChatCompletionSystemMessageParam.builder()
                                .content(PromptMessage.SYSTEM_PROMPT_PROJECT_DESCRIPTION)
                                .build()
                ),
                ChatCompletionMessageParam.ofUser(
                        ChatCompletionUserMessageParam.builder()
                                .content("Generate a project description for the following title: " + projectTitle)
                                .build()
                )
        );
    }

    private String fallbackProjectDescription(String projectTitle, Throwable t) {
        String correlationId = UUID.randomUUID().toString();
        log.error("AI call failed for projectTitle='{}', correlationId='{}'.", projectTitle, correlationId, t);
        return String.format(FALLBACK_MESSAGE_TEMPLATE, correlationId);
    }
}
