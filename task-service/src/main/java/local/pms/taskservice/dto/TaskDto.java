package local.pms.taskservice.dto;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

import local.pms.taskservice.type.TaskStatusType;
import local.pms.taskservice.type.TaskPriorityType;

public record TaskDto(
        String id,

        @NotBlank(message = "Title is required")
        @Size(min = 3, max = 255, message = "Title must be between 3 and 255 characters")
        String title,

        @NotBlank(message = "Description is required")
        @Size(min = 3, max = 255, message = "Description must be between 3 and 255 characters")
        String description,

        @NotNull(message = "Task status is required")
        TaskStatusType taskStatusType,

        @NotNull(message = "Task priority is required")
        TaskPriorityType taskPriorityType,

        boolean active,

        @NotBlank(message = "Project identifier is required")
        String projectId,

        String userId
) {}
