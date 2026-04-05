package local.pms.projectservice.dto;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

import local.pms.projectservice.type.ProjectStatusType;

import java.time.LocalDateTime;

import java.util.UUID;

public record ProjectDto(
        UUID id,

        @NotBlank(message = "Title is required")
        @Size(min = 3, max = 255, message = "Title must be between 3 and 255 characters")
        String title,

        @NotBlank(message = "Description is required")
        @Size(min = 3, max = 255, message = "Description must be between 3 and 255 characters")
        String description,

        @NotNull(message = "Project status is required")
        ProjectStatusType projectStatusType,

        @NotNull(message = "Start date is required")
        LocalDateTime startDate,

        @NotNull(message = "End date is required")
        LocalDateTime endDate,

        // Server-assigned from JWT; not validated in requests
        UUID userId
) {}
