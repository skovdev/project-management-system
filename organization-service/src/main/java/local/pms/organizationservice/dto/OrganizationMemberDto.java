package local.pms.organizationservice.dto;

import local.pms.organizationservice.type.OrganizationRoleType;

import java.util.UUID;

public record OrganizationMemberDto(
        UUID id,
        UUID organizationId,
        UUID userId,
        OrganizationRoleType role
) {}
