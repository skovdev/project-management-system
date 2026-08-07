package local.pms.taskservice.type;

/**
 * Local mirror of organization-service's role enum (OWNER/ADMIN/MEMBER).
 * Cross-service contracts are duplicated rather than shared in this codebase.
 */
public enum OrganizationRoleType {
    OWNER, ADMIN, MEMBER;

    /**
     * Declaration order above is privilege order (OWNER highest, MEMBER lowest), so a lower
     * ordinal means a more privileged role.
     */
    public boolean isAtLeast(OrganizationRoleType required) {
        return this.ordinal() <= required.ordinal();
    }
}
