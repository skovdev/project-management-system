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
    @DisplayName("findAllByUserId returns only projects belonging to the given userId")
    void should_returnProjectsForUser_when_findAllByUserId() {
        var userId = UUID.randomUUID();
        var otherUserId = UUID.randomUUID();
        persistProject("Project A", userId);
        persistProject("Project B", userId);
        persistProject("Project C", otherUserId);
        entityManager.flush();

        var page = projectRepository.findAllByUserId(userId, PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).allMatch(p -> p.getUserId().equals(userId));
    }

    @Test
    @DisplayName("findAllByUserId returns empty page when no projects for userId")
    void should_returnEmptyPage_when_noProjectsForUserId() {
        var page = projectRepository.findAllByUserId(UUID.randomUUID(), PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(0);
    }

    @Test
    @DisplayName("findByIdAndUserId returns project when both id and userId match")
    void should_returnProject_when_idAndUserIdMatch() {
        var userId = UUID.randomUUID();
        var saved = persistProject("My Project", userId);
        entityManager.flush();

        var result = projectRepository.findByIdAndUserId(saved.getId(), userId);

        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("My Project");
    }

    @Test
    @DisplayName("findByIdAndUserId returns empty when userId does not match")
    void should_returnEmpty_when_userIdDoesNotMatch() {
        var userId = UUID.randomUUID();
        var saved = persistProject("My Project", userId);
        entityManager.flush();

        var result = projectRepository.findByIdAndUserId(saved.getId(), UUID.randomUUID());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByIdAndUserId returns empty when project id does not exist")
    void should_returnEmpty_when_projectIdDoesNotExist() {
        var result = projectRepository.findByIdAndUserId(UUID.randomUUID(), UUID.randomUUID());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("deleteById soft-deletes project so it is no longer found by findById")
    void should_hideProjectFromFindById_when_softDeleted() {
        var userId = UUID.randomUUID();
        var saved = persistProject("To Delete", userId);
        entityManager.flush();

        projectRepository.deleteById(saved.getId());
        entityManager.flush();
        entityManager.clear();

        var result = projectRepository.findById(saved.getId());
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("deleteById soft-deletes project so it is no longer found by findByIdAndUserId")
    void should_hideProjectFromFindByIdAndUserId_when_softDeleted() {
        var userId = UUID.randomUUID();
        var saved = persistProject("To Delete", userId);
        entityManager.flush();

        projectRepository.deleteById(saved.getId());
        entityManager.flush();
        entityManager.clear();

        var result = projectRepository.findByIdAndUserId(saved.getId(), userId);
        assertThat(result).isEmpty();
    }

    private Project persistProject(String title, UUID userId) {
        var project = new Project();
        project.setTitle(title);
        project.setDescription("A description");
        project.setProjectStatusType(ProjectStatusType.PLANNING);
        project.setStartDate(LocalDateTime.of(2026, 1, 1, 0, 0));
        project.setEndDate(LocalDateTime.of(2026, 12, 31, 0, 0));
        project.setUserId(userId);
        project.setDeleted(false);
        return entityManager.persist(project);
    }
}
