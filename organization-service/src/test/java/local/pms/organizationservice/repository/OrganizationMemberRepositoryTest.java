package local.pms.organizationservice.repository;

import local.pms.organizationservice.entity.Organization;
import local.pms.organizationservice.entity.OrganizationMember;

import local.pms.organizationservice.type.OrganizationRoleType;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;

import org.springframework.data.domain.PageRequest;

import org.testcontainers.containers.PostgreSQLContainer;

import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = Replace.NONE)
class OrganizationMemberRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private OrganizationMemberRepository organizationMemberRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("findAllByOrganizationId returns only members of the given organization")
    void should_returnMembersForOrganization_when_findAllByOrganizationId() {
        var orgA = persistOrganization("Org A", UUID.randomUUID());
        var orgB = persistOrganization("Org B", UUID.randomUUID());
        persistMember(orgA, UUID.randomUUID(), OrganizationRoleType.OWNER);
        persistMember(orgA, UUID.randomUUID(), OrganizationRoleType.MEMBER);
        persistMember(orgB, UUID.randomUUID(), OrganizationRoleType.OWNER);
        entityManager.flush();

        var page = organizationMemberRepository.findAllByOrganizationId(orgA.getId(), PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("findByOrganizationIdAndUserId returns member when both match")
    void should_returnMember_when_organizationIdAndUserIdMatch() {
        var organization = persistOrganization("Org A", UUID.randomUUID());
        var userId = UUID.randomUUID();
        persistMember(organization, userId, OrganizationRoleType.OWNER);
        entityManager.flush();

        var result = organizationMemberRepository.findByOrganizationIdAndUserId(organization.getId(), userId);

        assertThat(result).isPresent();
        assertThat(result.get().getRole()).isEqualTo(OrganizationRoleType.OWNER);
    }

    @Test
    @DisplayName("findByOrganizationIdAndUserId returns empty when user is not a member")
    void should_returnEmpty_when_userNotAMember() {
        var organization = persistOrganization("Org A", UUID.randomUUID());
        entityManager.flush();

        var result = organizationMemberRepository.findByOrganizationIdAndUserId(organization.getId(), UUID.randomUUID());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("existsByOrganizationIdAndUserId returns true when membership exists")
    void should_returnTrue_when_membershipExists() {
        var organization = persistOrganization("Org A", UUID.randomUUID());
        var userId = UUID.randomUUID();
        persistMember(organization, userId, OrganizationRoleType.MEMBER);
        entityManager.flush();

        assertThat(organizationMemberRepository.existsByOrganizationIdAndUserId(organization.getId(), userId)).isTrue();
    }

    @Test
    @DisplayName("countByOrganizationIdAndRole counts only members with the given role")
    void should_countMembersWithGivenRole() {
        var organization = persistOrganization("Org A", UUID.randomUUID());
        persistMember(organization, UUID.randomUUID(), OrganizationRoleType.OWNER);
        persistMember(organization, UUID.randomUUID(), OrganizationRoleType.OWNER);
        persistMember(organization, UUID.randomUUID(), OrganizationRoleType.MEMBER);
        entityManager.flush();

        assertThat(organizationMemberRepository.countByOrganizationIdAndRole(organization.getId(), OrganizationRoleType.OWNER)).isEqualTo(2);
    }

    @Test
    @DisplayName("deleteById soft-deletes membership so it is no longer found by findByOrganizationIdAndUserId")
    void should_hideMemberFromFind_when_softDeleted() {
        var organization = persistOrganization("Org A", UUID.randomUUID());
        var userId = UUID.randomUUID();
        var member = persistMember(organization, userId, OrganizationRoleType.MEMBER);
        entityManager.flush();

        organizationMemberRepository.deleteById(member.getId());
        entityManager.flush();
        entityManager.clear();

        assertThat(organizationMemberRepository.findByOrganizationIdAndUserId(organization.getId(), userId)).isEmpty();
    }

    @Test
    @DisplayName("deleteAllByOrganizationId soft-deletes every member of the given organization only")
    void should_softDeleteAllMembers_when_deleteAllByOrganizationId() {
        var orgA = persistOrganization("Org A", UUID.randomUUID());
        var orgB = persistOrganization("Org B", UUID.randomUUID());
        var userInA1 = UUID.randomUUID();
        var userInA2 = UUID.randomUUID();
        var userInB = UUID.randomUUID();
        persistMember(orgA, userInA1, OrganizationRoleType.OWNER);
        persistMember(orgA, userInA2, OrganizationRoleType.MEMBER);
        persistMember(orgB, userInB, OrganizationRoleType.OWNER);
        entityManager.flush();

        organizationMemberRepository.deleteAllByOrganizationId(orgA.getId());
        entityManager.flush();
        entityManager.clear();

        assertThat(organizationMemberRepository.findByOrganizationIdAndUserId(orgA.getId(), userInA1)).isEmpty();
        assertThat(organizationMemberRepository.findByOrganizationIdAndUserId(orgA.getId(), userInA2)).isEmpty();
        assertThat(organizationMemberRepository.findByOrganizationIdAndUserId(orgB.getId(), userInB)).isPresent();
    }

    private Organization persistOrganization(String name, UUID ownerId) {
        var organization = new Organization();
        organization.setName(name);
        organization.setDescription("A description");
        organization.setOwnerId(ownerId);
        organization.setDeleted(false);
        return entityManager.persist(organization);
    }

    private OrganizationMember persistMember(Organization organization, UUID userId, OrganizationRoleType role) {
        var member = new OrganizationMember();
        member.setOrganization(organization);
        member.setUserId(userId);
        member.setRole(role);
        member.setDeleted(false);
        return entityManager.persist(member);
    }
}
