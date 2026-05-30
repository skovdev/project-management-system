package local.pms.notificationservice.event;

import java.util.UUID;

public record ProjectCreatedEvent(UUID projectId, UUID userId, String title) {}
