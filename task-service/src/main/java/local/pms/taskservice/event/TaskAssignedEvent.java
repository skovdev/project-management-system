package local.pms.taskservice.event;

import java.util.UUID;

public record TaskAssignedEvent(UUID taskId, UUID assigneeId, String title) {}
