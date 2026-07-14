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
class OrganizationRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("findAllByMemberUserId returns only organizations the given user is a member of")
    void should_returnOrganizationsForUser_when_findAllByMemberUserId() {
        var userId = UUID.randomUUID();
        var otherUserId = UUID.randomUUID();
        var orgA = persistOrganization("Org A", userId);
        var orgB = persistOrganization("Org B", userId);
        var orgC = persistOrganization("Org C", otherUserId);
        persistMember(orgA, userId, OrganizationRoleType.OWNER);
        persistMember(orgB, userId, OrganizationRoleType.OWNER);
        persistMember(orgC, otherUserId, OrganizationRoleType.OWNER);
        entityManager.flush();

        var page = organizationRepository.findAllByMemberUserId(userId, PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("findAllByMemberUserId returns empty page when the user has no memberships")
    void should_returnEmptyPage_when_noMemberships() {
        var page = organizationRepository.findAllByMemberUserId(UUID.randomUUID(), PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(0);
    }

    @Test
    @DisplayName("deleteById soft-deletes organization so it is no longer found by findById")
    void should_hideOrganizationFromFindById_when_softDeleted() {
        var userId = UUID.randomUUID();
        var organization = persistOrganization("To Delete", userId);
        entityManager.flush();

        organizationRepository.deleteById(organization.getId());
        entityManager.flush();
        entityManager.clear();

        assertThat(organizationRepository.findById(organization.getId())).isEmpty();
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
