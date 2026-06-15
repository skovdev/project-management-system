package local.pms.aiservice.dto.api.request.project;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;

/**
 * Request payload for project-description generation.
 * Callers supply only the project title; the prompt is owned by ai-service.
 */
@Schema(description = "Request for AI-generated project description")
public record ProjectDescriptionRequestDto(
        @Schema(description = "Project title", example = "Project Management System")
        @NotBlank(message = "Title must not be blank")
        String title
) {}
