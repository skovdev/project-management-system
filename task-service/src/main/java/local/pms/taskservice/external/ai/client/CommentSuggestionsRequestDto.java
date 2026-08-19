package local.pms.taskservice.external.ai.client;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

/**
 * Request payload sent to the ai-service comment-suggestions endpoint.
 * Contains only domain data; the AI prompt is owned by ai-service.
 */
public record CommentSuggestionsRequestDto(
        @NotBlank String taskTitle,
        @NotBlank String taskDescription,
        @NotBlank String commentContent,
        List<String> threadContext
) {}
