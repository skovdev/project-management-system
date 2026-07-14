package local.pms.projectservice.repository;

import local.pms.projectservice.entity.Project;

import local.pms.projectservice.type.ProjectStatusType;

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

import java.time.LocalDateTime;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = Replace.NONE)
class ProjectRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("findAllByOrganizationId returns only projects belonging to the given organization")
    void should_returnProjectsForOrganization_when_findAllByOrganizationId() {
        var organizationId = UUID.randomUUID();
        var otherOrganizationId = UUID.randomUUID();
        persistProject("Project A", organizationId);
        persistProject("Project B", organizationId);
        persistProject("Project C", otherOrganizationId);
        entityManager.flush();

        var page = projectRepository.findAllByOrganizationId(organizationId, PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).allMatch(p -> p.getOrganizationId().equals(organizationId));
    }

    @Test
    @DisplayName("findAllByOrganizationId returns empty page when no projects for organizationId")
    void should_returnEmptyPage_when_noProjectsForOrganizationId() {
        var page = projectRepository.findAllByOrganizationId(UUID.randomUUID(), PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(0);
    }

    @Test
    @DisplayName("findByIdAndOrganizationId returns project when both id and organizationId match")
    void should_returnProject_when_idAndOrganizationIdMatch() {
        var organizationId = UUID.randomUUID();
        var saved = persistProject("My Project", organizationId);
        entityManager.flush();

        var result = projectRepository.findByIdAndOrganizationId(saved.getId(), organizationId);

        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("My Project");
    }

    @Test
    @DisplayName("findByIdAndOrganizationId returns empty when organizationId does not match")
    void should_returnEmpty_when_organizationIdDoesNotMatch() {
        var organizationId = UUID.randomUUID();
        var saved = persistProject("My Project", organizationId);
        entityManager.flush();

        var result = projectRepository.findByIdAndOrganizationId(saved.getId(), UUID.randomUUID());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByIdAndOrganizationId returns empty when project id does not exist")
    void should_returnEmpty_when_projectIdDoesNotExist() {
        var result = projectRepository.findByIdAndOrganizationId(UUID.randomUUID(), UUID.randomUUID());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("deleteById soft-deletes project so it is no longer found by findById")
    void should_hideProjectFromFindById_when_softDeleted() {
        var organizationId = UUID.randomUUID();
        var saved = persistProject("To Delete", organizationId);
        entityManager.flush();

        projectRepository.deleteById(saved.getId());
        entityManager.flush();
        entityManager.clear();

        var result = projectRepository.findById(saved.getId());
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("deleteById soft-deletes project so it is no longer found by findByIdAndOrganizationId")
    void should_hideProjectFromFindByIdAndOrganizationId_when_softDeleted() {
        var organizationId = UUID.randomUUID();
        var saved = persistProject("To Delete", organizationId);
        entityManager.flush();

        projectRepository.deleteById(saved.getId());
        entityManager.flush();
        entityManager.clear();

        var result = projectRepository.findByIdAndOrganizationId(saved.getId(), organizationId);
        assertThat(result).isEmpty();
    }

    private Project persistProject(String title, UUID organizationId) {
        var project = new Project();
        project.setTitle(title);
        project.setDescription("A description");
        project.setProjectStatusType(ProjectStatusType.PLANNING);
        project.setStartDate(LocalDateTime.of(2026, 1, 1, 0, 0));
        project.setEndDate(LocalDateTime.of(2026, 12, 31, 0, 0));
        project.setUserId(UUID.randomUUID());
        project.setOrganizationId(organizationId);
        project.setDeleted(false);
        return entityManager.persist(project);
    }
}
