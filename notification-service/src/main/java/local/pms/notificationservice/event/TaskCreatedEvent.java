package local.pms.notificationservice.event;

import java.util.UUID;

public record TaskCreatedEvent(UUID taskId, UUID userId, String title) {}
