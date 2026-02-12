package local.pms.projectservice.dto;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;

import local.pms.projectservice.type.ProjectStatusType;

import java.time.LocalDateTime;

import java.util.UUID;

public record ProjectDto (
        @NotBlank(message = "The id field is required")
        UUID id,
        @NotBlank(message = "The title field is required")
        @Size(min = 3, max = 255, message = "The title field must be between 3 and 255 characters")
        String title,
        @NotBlank(message = "The description field is required")
        @Size(min = 3, max = 255, message = "The description field must be between 3 and 255 characters")
        String description,
        @NotBlank(message = "The projectStatusType field is required")
        ProjectStatusType projectStatusType,

        @NotBlank(message = "The startDate field is required")
        LocalDateTime startDate,
        @NotBlank(message = "The endDate field is required")
        LocalDateTime endDate,
        @NotBlank(message = "The userId field is required")
        UUID userId
) {}