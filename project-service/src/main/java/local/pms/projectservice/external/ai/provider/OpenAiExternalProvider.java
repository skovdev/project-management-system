package local.pms.projectservice.external.ai.provider;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

import io.github.resilience4j.retry.annotation.Retry;

import local.pms.projectservice.exception.DescriptionGenerationException;

import local.pms.projectservice.external.ai.client.AiFeignClient;
import local.pms.projectservice.external.ai.client.ProjectDescriptionRequestDto;

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

    private final AiFeignClient aiFeignClient;

    @Override
    @CircuitBreaker(name = "projectDescriptionAiGeneration", fallbackMethod = "fallbackProjectDescription")
    @Retry(name = "projectDescriptionAiGeneration", fallbackMethod = "fallbackProjectDescription")
    public String generateProjectDescription(String projectTitle) {
        var response = aiFeignClient.generateProjectDescription(
                new ProjectDescriptionRequestDto(projectTitle));
        if (response == null) {
            log.warn("AI service returned a null response for projectTitle='{}'", projectTitle);
            throw new DescriptionGenerationException(
                    "AI service returned a null response for projectTitle='" + projectTitle + "'");
        }
        if (response.getErrors() != null && !response.getErrors().isEmpty()) {
            log.warn("AI service returned errors for projectTitle='{}': {}", projectTitle, response.getErrors());
            throw new DescriptionGenerationException(
                    "AI service returned errors for projectTitle='" + projectTitle + "': " + response.getErrors());
        }
        var description = response.getData();
        if (description == null || description.isBlank()) {
            log.warn("AI service returned an empty description for projectTitle='{}'", projectTitle);
            throw new DescriptionGenerationException(
                    "AI service returned an empty description for projectTitle='" + projectTitle + "'");
        }
        return description;
    }

    private String fallbackProjectDescription(String projectTitle, Throwable t) {
        log.error("AI call failed for projectTitle='{}'.", projectTitle, t);
        throw new DescriptionGenerationException("Project description generation is temporarily unavailable", t);
    }
}
