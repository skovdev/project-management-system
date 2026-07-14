package local.pms.projectservice.external.organization.provider;

import local.pms.projectservice.type.OrganizationRoleType;

import java.util.UUID;

public interface OrganizationAccessProvider {

    /**
     * Verifies that the calling user is a member of the given organization.
     *
     * @param organizationId the organization identifier
     * @return the caller's role in that organization
     * @throws local.pms.projectservice.exception.ProjectAccessDeniedException if the caller
     *         is not a member, or membership could not be verified (fails closed)
     */
    OrganizationRoleType verifyMembership(UUID organizationId);
}
