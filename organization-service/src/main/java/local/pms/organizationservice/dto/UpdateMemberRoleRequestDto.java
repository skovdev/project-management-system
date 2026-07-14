package local.pms.organizationservice.dto;

import jakarta.validation.constraints.NotNull;

import local.pms.organizationservice.type.OrganizationRoleType;

public record UpdateMemberRoleRequestDto(
        @NotNull(message = "Role is required")
        OrganizationRoleType role
) {}
