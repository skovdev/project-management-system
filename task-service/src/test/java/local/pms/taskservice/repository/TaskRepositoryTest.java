package local.pms.taskservice.repository;

import local.pms.taskservice.entity.Task;

import local.pms.taskservice.type.TaskStatusType;
import local.pms.taskservice.type.TaskPriorityType;

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
class TaskRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("findAllByUserId returns only tasks belonging to the given userId")
    void should_returnTasksForUser_when_findAllByUserId() {
        var userId = UUID.randomUUID();
        var otherUserId = UUID.randomUUID();
        var projectId = UUID.randomUUID();
        persistTask("Task A", userId, projectId);
        persistTask("Task B", userId, projectId);
        persistTask("Task C", otherUserId, projectId);
        entityManager.flush();

        var page = taskRepository.findAllByUserId(userId, PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).allMatch(t -> t.getUserId().equals(userId));
    }

    @Test
    @DisplayName("findAllByUserId returns empty page when no tasks for userId")
    void should_returnEmptyPage_when_noTasksForUserId() {
        var page = taskRepository.findAllByUserId(UUID.randomUUID(), PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(0);
    }

    @Test
    @DisplayName("findByIdAndUserId returns task when both id and userId match")
    void should_returnTask_when_idAndUserIdMatch() {
        var userId = UUID.randomUUID();
        var saved = persistTask("My Task", userId, UUID.randomUUID());
        entityManager.flush();

        var result = taskRepository.findByIdAndUserId(saved.getId(), userId);

        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("My Task");
    }

    @Test
    @DisplayName("findByIdAndUserId returns empty when userId does not match")
    void should_returnEmpty_when_userIdDoesNotMatch() {
        var userId = UUID.randomUUID();
        var saved = persistTask("My Task", userId, UUID.randomUUID());
        entityManager.flush();

        var result = taskRepository.findByIdAndUserId(saved.getId(), UUID.randomUUID());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByIdAndUserId returns empty when task id does not exist")
    void should_returnEmpty_when_taskIdDoesNotExist() {
        var result = taskRepository.findByIdAndUserId(UUID.randomUUID(), UUID.randomUUID());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("deleteAllByProjectId removes all tasks for the given projectId")
    void should_deleteAllTasks_when_deleteAllByProjectId() {
        var userId = UUID.randomUUID();
        var projectId = UUID.randomUUID();
        persistTask("Task 1", userId, projectId);
        persistTask("Task 2", userId, projectId);
        persistTask("Other Task", userId, UUID.randomUUID());
        entityManager.flush();

        taskRepository.deleteAllByProjectId(projectId);
        entityManager.flush();
        entityManager.clear();

        var remaining = taskRepository.findAllByUserId(userId, PageRequest.of(0, 10));
        assertThat(remaining.getTotalElements()).isEqualTo(1);
        assertThat(remaining.getContent().get(0).getTitle()).isEqualTo("Other Task");
    }

    @Test
    @DisplayName("deleteById soft-deletes task so it is no longer found by findById")
    void should_hideTaskFromFindById_when_softDeleted() {
        var userId = UUID.randomUUID();
        var saved = persistTask("To Delete", userId, UUID.randomUUID());
        entityManager.flush();

        taskRepository.deleteById(saved.getId());
        entityManager.flush();
        entityManager.clear();

        var result = taskRepository.findById(saved.getId());
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("deleteById soft-deletes task so it is no longer found by findByIdAndUserId")
    void should_hideTaskFromFindByIdAndUserId_when_softDeleted() {
        var userId = UUID.randomUUID();
        var saved = persistTask("To Delete", userId, UUID.randomUUID());
        entityManager.flush();

        taskRepository.deleteById(saved.getId());
        entityManager.flush();
        entityManager.clear();

        var result = taskRepository.findByIdAndUserId(saved.getId(), userId);
        assertThat(result).isEmpty();
    }

    private Task persistTask(String title, UUID userId, UUID projectId) {
        var task = new Task();
        task.setTitle(title);
        task.setDescription("A description");
        task.setTaskStatusType(TaskStatusType.TODO);
        task.setTaskPriorityType(TaskPriorityType.MEDIUM);
        task.setActive(true);
        task.setProjectId(projectId);
        task.setUserId(userId);
        task.setDeleted(false);
        return entityManager.persist(task);
    }
}
