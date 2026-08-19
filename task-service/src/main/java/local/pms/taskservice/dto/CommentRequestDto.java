package local.pms.taskservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

@Schema(description = "Request payload for creating or updating a comment")
public record CommentRequestDto(

        @Schema(description = "Comment text content", example = "This task needs clarification on the acceptance criteria.")
        @NotBlank(message = "Content is required")
        @Size(min = 1, max = 2000, message = "Content must be between 1 and 2000 characters")
        String content,

        @Schema(description = "Identifier of the comment being replied to; omit for a top-level comment. " +
                               "Replying to a reply automatically attaches the new comment to that reply's top-level parent.")
        UUID parentCommentId

) {

    /**
     * Convenience constructor for a top-level comment (no reply target).
     */
    public CommentRequestDto(String content) {
        this(content, null);
    }
}
