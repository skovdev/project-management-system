package local.pms.taskservice.event;

import java.util.UUID;

public record TaskCreatedEvent(UUID taskId, UUID userId, String title) {}
