package local.pms.taskservice.external.project.client;

import java.util.UUID;

/**
 * Local mirror of project-service's ProjectOrganizationDto response shape.
 */
public record ProjectOrganizationResponseDto(UUID organizationId) {}
