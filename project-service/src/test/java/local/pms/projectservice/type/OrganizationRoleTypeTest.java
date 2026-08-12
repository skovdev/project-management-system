package local.pms.projectservice.type;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrganizationRoleTypeTest {

    @Test
    void should_beAtLeastEveryRole_when_callerIsOwner() {
        assertThat(OrganizationRoleType.OWNER.isAtLeast(OrganizationRoleType.OWNER)).isTrue();
        assertThat(OrganizationRoleType.OWNER.isAtLeast(OrganizationRoleType.ADMIN)).isTrue();
        assertThat(OrganizationRoleType.OWNER.isAtLeast(OrganizationRoleType.MEMBER)).isTrue();
    }

    @Test
    void should_beAtLeastAdminButNotOwner_when_callerIsAdmin() {
        assertThat(OrganizationRoleType.ADMIN.isAtLeast(OrganizationRoleType.OWNER)).isFalse();
        assertThat(OrganizationRoleType.ADMIN.isAtLeast(OrganizationRoleType.ADMIN)).isTrue();
        assertThat(OrganizationRoleType.ADMIN.isAtLeast(OrganizationRoleType.MEMBER)).isTrue();
    }

    @Test
    void should_onlyBeAtLeastMember_when_callerIsMember() {
        assertThat(OrganizationRoleType.MEMBER.isAtLeast(OrganizationRoleType.OWNER)).isFalse();
        assertThat(OrganizationRoleType.MEMBER.isAtLeast(OrganizationRoleType.ADMIN)).isFalse();
        assertThat(OrganizationRoleType.MEMBER.isAtLeast(OrganizationRoleType.MEMBER)).isTrue();
    }
}
