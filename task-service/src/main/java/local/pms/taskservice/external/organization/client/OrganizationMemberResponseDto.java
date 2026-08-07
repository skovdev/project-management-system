package local.pms.taskservice.external.organization.client;

import local.pms.taskservice.type.OrganizationRoleType;

import java.util.UUID;

/**
 * Local mirror of organization-service's OrganizationMemberDto response shape.
 */
public record OrganizationMemberResponseDto(
        UUID id,
        UUID organizationId,
        UUID userId,
        OrganizationRoleType role
) {}
