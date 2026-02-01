package local.pms.projectservice.external.ai.provider;

import com.openai.models.chat.completions.ChatCompletionMessageParam;
import com.openai.models.chat.completions.ChatCompletionUserMessageParam;
import com.openai.models.chat.completions.ChatCompletionSystemMessageParam;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

import io.github.resilience4j.retry.annotation.Retry;

import local.pms.projectservice.external.ai.client.AiFeignClient;

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
        return aiFeignClient.generateProjectDescription(fillChatGptMessages(projectTitle));
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
