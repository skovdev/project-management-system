package local.pms.aiservice.dto.api.request.task;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

/**
 * Request payload for comment reply-suggestion generation.
 * Callers supply the task context, the comment to reply to, and optional prior thread
 * comments for additional context; the prompt is owned by ai-service.
 */
@Schema(description = "Request for AI-generated comment reply suggestions")
public record CommentSuggestionsRequestDto(

        @Schema(description = "Title of the task the comment belongs to", example = "Implement user login")
        @NotBlank(message = "Task title must not be blank")
        String taskTitle,

        @Schema(description = "Description of the task the comment belongs to", example = "Allow users to log in using email and password")
        @NotBlank(message = "Task description must not be blank")
        String taskDescription,

        @Schema(description = "Content of the comment to generate reply suggestions for", example = "I think we should use OAuth instead of a custom login flow.")
        @NotBlank(message = "Comment content must not be blank")
        String commentContent,

        @Schema(description = "Recent prior comments on the same task, used as additional thread context; may be null or empty")
        List<String> threadContext

) {}
