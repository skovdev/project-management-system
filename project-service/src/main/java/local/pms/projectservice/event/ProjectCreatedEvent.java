package local.pms.projectservice.event;

import java.util.UUID;

public record ProjectCreatedEvent(UUID projectId, UUID userId, String title) {}
