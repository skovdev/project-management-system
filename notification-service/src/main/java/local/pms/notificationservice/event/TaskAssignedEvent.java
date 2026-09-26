package local.pms.notificationservice.event;

import java.util.UUID;

public record TaskAssignedEvent(UUID taskId, UUID assigneeId, String title) {}
