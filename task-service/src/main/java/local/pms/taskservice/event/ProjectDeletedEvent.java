package local.pms.taskservice.event;

import java.util.UUID;

public record ProjectDeletedEvent(UUID projectId) {}
