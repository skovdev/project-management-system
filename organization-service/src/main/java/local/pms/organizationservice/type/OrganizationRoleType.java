package local.pms.organizationservice.type;

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
