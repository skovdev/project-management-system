package local.pms.taskservice.dto;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;

import local.pms.taskservice.type.TaskStatusType;
import local.pms.taskservice.type.TaskPriorityType;

public record TaskDto(
        @NotBlank(message = "The id field is required")
        String id,
        @NotBlank(message = "The title field is required")
        @Size(min = 3, max = 255, message = "The title field must be between 3 and 255 characters")
        String title,
        @NotBlank(message = "The description field is required")
        @Size(min = 3, max = 255, message = "The description field must be between 3 and 255 characters")
        String description,
        @NotBlank(message = "The taskStatusType field is required")
        TaskStatusType taskStatusType,
        @NotBlank(message = "The taskPriorityType field is required")
        TaskPriorityType taskPriorityType,
        @NotBlank(message = "The active field is required")
        boolean active,
        @NotBlank(message = "The projectId field is required")
        String projectId,
        @NotBlank(message = "The userId field is required")
        String userId
) {}