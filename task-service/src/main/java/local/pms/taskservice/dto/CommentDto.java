package local.pms.taskservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

import java.util.List;

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

        @Schema(description = "Identifier of the top-level comment this is a reply to; null for top-level comments")
        String parentCommentId,

        @Schema(description = "Timestamp when the comment was created")
        Instant createdAt,

        @Schema(description = "Timestamp when the comment was last updated")
        Instant updatedAt,

        @Schema(description = "Replies to this comment, ordered oldest first; always empty for a reply itself")
        List<CommentDto> replies

) {

    /**
     * Returns a copy of this DTO with {@code replies} set to the given list.
     * Used to attach replies after entity-to-DTO mapping, since replies are assembled
     * by the service rather than mapped directly from a JPA association.
     */
    public CommentDto withReplies(List<CommentDto> replies) {
        return new CommentDto(id, content, taskId, authorId, parentCommentId, createdAt, updatedAt, replies);
    }
}
