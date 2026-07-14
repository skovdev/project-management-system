package local.pms.organizationservice.event;

import java.util.UUID;

public record OrganizationDeletedEvent(UUID organizationId) {}
