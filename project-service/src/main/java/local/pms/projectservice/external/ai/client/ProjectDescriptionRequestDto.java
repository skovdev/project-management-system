package local.pms.projectservice.external.ai.client;

import jakarta.validation.constraints.NotBlank;

/**
 * Request payload sent to the ai-service project-description endpoint.
 * Contains only domain data; the AI prompt is owned by ai-service.
 */
public record ProjectDescriptionRequestDto(
        @NotBlank String title
) {}
