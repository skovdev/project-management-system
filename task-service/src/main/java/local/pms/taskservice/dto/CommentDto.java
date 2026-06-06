package local.pms.taskservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Task comment details")
public record CommentDto(

        @Schema(description = "Comment unique identifier")
        String id,

        @Schema(description = "Comment text content")
        String content,

        @Schema(description = "Identifier of the task this comment belongs to")
        String taskId,

        @Schema(description = "Identifier of the user who authored this comment")
        String authorId,

        @Schema(description = "Timestamp when the comment was created")
        Instant createdAt,

        @Schema(description = "Timestamp when the comment was last updated")
        Instant updatedAt

) {}
