package local.pms.aiservice.dto.api.request.task;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;

/**
 * Request payload for acceptance-criteria generation.
 * Callers supply the task title and description; the prompt is owned by ai-service.
 */
@Schema(description = "Request for AI-generated acceptance criteria")
public record AcceptanceCriteriaRequestDto(

        @Schema(description = "Task title", example = "Implement user login")
        @NotBlank(message = "Title must not be blank")
        String title,

        @Schema(description = "Task description", example = "Allow users to log in using email and password")
        @NotBlank(message = "Description must not be blank")
        String description

) {}
