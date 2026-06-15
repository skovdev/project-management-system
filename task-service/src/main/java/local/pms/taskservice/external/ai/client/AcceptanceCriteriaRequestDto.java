package local.pms.taskservice.external.ai.client;

import jakarta.validation.constraints.NotBlank;

/**
 * Request payload sent to the ai-service acceptance-criteria endpoint.
 * Contains only domain data; the AI prompt is owned by ai-service.
 */
public record AcceptanceCriteriaRequestDto(
        @NotBlank String title,
        @NotBlank String description
) {}
