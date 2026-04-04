package local.pms.projectservice.event;

import java.util.UUID;

public record ProjectDeletedEvent(UUID projectId) {}
