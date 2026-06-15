package local.pms.taskservice.external.ai.provider;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

import io.github.resilience4j.retry.annotation.Retry;

import local.pms.taskservice.exception.AcceptanceCriteriaGenerationException;

import local.pms.taskservice.external.ai.client.AiFeignClient;
import local.pms.taskservice.external.ai.client.AcceptanceCriteriaRequestDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * OpenAI-backed implementation of {@link AiExternalProvider} that delegates to the ai-service
 * via Feign, protected by a circuit breaker and retry policy.
 * The ai-service owns the prompt; this provider sends only domain data.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OpenAiExternalProvider implements AiExternalProvider {

    private static final String FALLBACK_MESSAGE_TEMPLATE =
            "Acceptance criteria generation is temporarily unavailable. Reference ID: %s";

    private final AiFeignClient aiFeignClient;

    /**
     * {@inheritDoc}
     *
     * <p>Protected by Resilience4j circuit breaker and retry. Falls back to a static message
     * when the AI service is unreachable or returns an error.
     */
    @Override
    @CircuitBreaker(name = "taskAcceptanceCriteriaAiGeneration", fallbackMethod = "fallbackAcceptanceCriteria")
    @Retry(name = "taskAcceptanceCriteriaAiGeneration", fallbackMethod = "fallbackAcceptanceCriteria")
    public String generateAcceptanceCriteria(String taskTitle, String taskDescription) {
        var response = aiFeignClient.generateAcceptanceCriteria(
                new AcceptanceCriteriaRequestDto(taskTitle, taskDescription));
        if (response == null) {
            log.warn("AI service returned null response for taskTitle='{}'", taskTitle);
            throw new AcceptanceCriteriaGenerationException(
                    "AI service returned null response for taskTitle='" + taskTitle + "'");
        }
        if (response.getErrors() != null && !response.getErrors().isEmpty()) {
            log.warn("AI service returned errors for taskTitle='{}': {}", taskTitle, response.getErrors());
            throw new AcceptanceCriteriaGenerationException(
                    "AI service returned errors for taskTitle='" + taskTitle + "': " + response.getErrors());
        }
        var result = response.getData();
        if (result == null || result.isBlank()) {
            log.warn("AI service returned blank acceptance criteria for taskTitle='{}'", taskTitle);
            throw new AcceptanceCriteriaGenerationException(
                    "AI service returned blank acceptance criteria for taskTitle='" + taskTitle + "'");
        }
        return result;
    }

    private String fallbackAcceptanceCriteria(String taskTitle, String taskDescription, Throwable t) {
        var correlationId = UUID.randomUUID().toString();
        log.error("AI call failed for taskTitle='{}', correlationId='{}'.", taskTitle, correlationId, t);
        return String.format(FALLBACK_MESSAGE_TEMPLATE, correlationId);
    }
}
