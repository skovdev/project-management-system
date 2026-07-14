package local.pms.organizationservice.dto;

import jakarta.validation.constraints.NotNull;

import local.pms.organizationservice.type.OrganizationRoleType;

import java.util.UUID;

public record AddMemberRequestDto(
        @NotNull(message = "User identifier is required")
        UUID userId,

        @NotNull(message = "Role is required")
        OrganizationRoleType role
) {}
