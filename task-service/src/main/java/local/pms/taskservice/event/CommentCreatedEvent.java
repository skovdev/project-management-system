package local.pms.taskservice.event;

import java.util.UUID;

public record CommentCreatedEvent(UUID commentId, UUID taskId, UUID authorId, String content) {}
