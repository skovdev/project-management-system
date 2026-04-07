package local.pms.taskservice.service;

import local.pms.taskservice.config.jwt.JwtTokenProvider;

import local.pms.taskservice.dto.TaskDto;

import local.pms.taskservice.entity.Task;

import local.pms.taskservice.exception.TaskNotFoundException;
import local.pms.taskservice.exception.TaskAccessDeniedException;
import local.pms.taskservice.exception.InvalidTaskInputException;

import local.pms.taskservice.repository.TaskRepository;

import local.pms.taskservice.service.impl.TaskServiceImpl;

import local.pms.taskservice.type.TaskStatusType;
import local.pms.taskservice.type.TaskPriorityType;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.InjectMocks;

import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TokenService tokenService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private TaskServiceImpl taskService;

    @Test
    @DisplayName("create saves task and returns DTO with userId from token")
    void should_saveAndReturnDto_when_createWithValidData() {
        var userId = UUID.randomUUID();
        var dto = buildTaskDto(null);
        var saved = buildTask(UUID.randomUUID(), userId);

        stubToken(userId);
        when(taskRepository.save(any(Task.class))).thenReturn(saved);

        var result = taskService.create(dto);

        assertThat(result.title()).isEqualTo("My Task");
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    @DisplayName("create throws InvalidTaskInputException when DTO is null")
    void should_throwInvalidTaskInputException_when_createWithNullDto() {
        assertThatThrownBy(() -> taskService.create(null))
                .isInstanceOf(InvalidTaskInputException.class)
                .hasMessageContaining("cannot be null");

        verify(taskRepository, never()).save(any());
    }

    @Test
    @DisplayName("create throws TaskAccessDeniedException when token is null")
    void should_throwTaskAccessDeniedException_when_tokenIsNull() {
        when(tokenService.getToken()).thenReturn(null);

        assertThatThrownBy(() -> taskService.create(buildTaskDto(null)))
                .isInstanceOf(TaskAccessDeniedException.class)
                .hasMessageContaining("missing or invalid authentication token");
    }

    @Test
    @DisplayName("findAll returns page of DTOs filtered by userId from token")
    void should_returnPageOfDtos_when_findAll() {
        var userId = UUID.randomUUID();
        var pageable = PageRequest.of(0, 10);
        var task = buildTask(UUID.randomUUID(), userId);
        var page = new PageImpl<>(List.of(task), pageable, 1);

        stubToken(userId);
        when(taskRepository.findAllByUserId(userId, pageable)).thenReturn(page);

        var result = taskService.findAll(pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).title()).isEqualTo("My Task");
    }

    @Test
    @DisplayName("findById returns DTO when task exists for userId")
    void should_returnDto_when_findByIdExists() {
        var taskId = UUID.randomUUID();
        var userId = UUID.randomUUID();
        var task = buildTask(taskId, userId);

        stubToken(userId);
        when(taskRepository.findByIdAndUserId(taskId, userId)).thenReturn(Optional.of(task));

        var result = taskService.findById(taskId);

        assertThat(result.id()).isEqualTo(taskId.toString());
        assertThat(result.title()).isEqualTo("My Task");
    }

    @Test
    @DisplayName("findById throws TaskNotFoundException when task not found")
    void should_throwTaskNotFoundException_when_findByIdNotFound() {
        var taskId = UUID.randomUUID();
        var userId = UUID.randomUUID();

        stubToken(userId);
        when(taskRepository.findByIdAndUserId(taskId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.findById(taskId))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessageContaining(taskId.toString());
    }

    @Test
    @DisplayName("update returns updated DTO when caller owns the task")
    void should_returnUpdatedDto_when_callerOwnsTask() {
        var taskId = UUID.randomUUID();
        var userId = UUID.randomUUID();
        var existing = buildTask(taskId, userId);
        var updateDto = buildTaskDto(taskId.toString());

        stubToken(userId);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existing));
        when(taskRepository.save(existing)).thenReturn(existing);

        var result = taskService.update(taskId, updateDto);

        assertThat(result).isNotNull();
        verify(taskRepository).save(existing);
    }

    @Test
    @DisplayName("update throws InvalidTaskInputException when DTO is null")
    void should_throwInvalidTaskInputException_when_updateWithNullDto() {
        assertThatThrownBy(() -> taskService.update(UUID.randomUUID(), null))
                .isInstanceOf(InvalidTaskInputException.class)
                .hasMessageContaining("cannot be null");

        verify(taskRepository, never()).save(any());
    }

    @Test
    @DisplayName("update throws TaskNotFoundException when task not found")
    void should_throwTaskNotFoundException_when_updateNotFound() {
        var taskId = UUID.randomUUID();
        var userId = UUID.randomUUID();

        stubToken(userId);
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.update(taskId, buildTaskDto(taskId.toString())))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessageContaining(taskId.toString());

        verify(taskRepository, never()).save(any());
    }

    @Test
    @DisplayName("update throws TaskAccessDeniedException when caller does not own the task")
    void should_throwTaskAccessDeniedException_when_callerNotOwner() {
        var taskId = UUID.randomUUID();
        var ownerId = UUID.randomUUID();
        var callerId = UUID.randomUUID();
        var existing = buildTask(taskId, ownerId);

        stubToken(callerId);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> taskService.update(taskId, buildTaskDto(taskId.toString())))
                .isInstanceOf(TaskAccessDeniedException.class)
                .hasMessageContaining(taskId.toString());

        verify(taskRepository, never()).save(any());
    }

    @Test
    @DisplayName("update throws InvalidTaskInputException when projectId is blank")
    void should_throwInvalidTaskInputException_when_projectIdIsBlank() {
        var taskId = UUID.randomUUID();
        var userId = UUID.randomUUID();
        var existing = buildTask(taskId, userId);
        var badDto = new TaskDto(taskId.toString(), "My Task", "A task description",
                TaskStatusType.TODO, TaskPriorityType.MEDIUM, true, "  ", null);

        stubToken(userId);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> taskService.update(taskId, badDto))
                .isInstanceOf(InvalidTaskInputException.class)
                .hasMessageContaining("Project ID cannot be null or blank");

        verify(taskRepository, never()).save(any());
    }

    @Test
    @DisplayName("delete removes task when task exists")
    void should_deleteTask_when_taskExists() {
        var taskId = UUID.randomUUID();
        var task = buildTask(taskId, UUID.randomUUID());

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        taskService.delete(taskId);

        verify(taskRepository).deleteById(taskId);
    }

    @Test
    @DisplayName("delete throws TaskNotFoundException when task not found")
    void should_throwTaskNotFoundException_when_deleteNotFound() {
        var taskId = UUID.randomUUID();
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.delete(taskId))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessageContaining(taskId.toString());

        verify(taskRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("deleteAllByProjectId delegates to repository")
    void should_deleteAllTasks_when_deleteAllByProjectId() {
        var projectId = UUID.randomUUID();

        taskService.deleteAllByProjectId(projectId);

        verify(taskRepository).deleteAllByProjectId(projectId);
    }

    private void stubToken(UUID userId) {
        when(tokenService.getToken()).thenReturn("test-token");
        when(jwtTokenProvider.extractAuthUserId("test-token")).thenReturn(userId);
    }

    private Task buildTask(UUID id, UUID userId) {
        var task = new Task();
        task.setId(id);
        task.setTitle("My Task");
        task.setDescription("A task description");
        task.setTaskStatusType(TaskStatusType.TODO);
        task.setTaskPriorityType(TaskPriorityType.MEDIUM);
        task.setActive(true);
        task.setProjectId(UUID.randomUUID());
        task.setUserId(userId);
        task.setDeleted(false);
        return task;
    }

    private TaskDto buildTaskDto(String id) {
        return new TaskDto(
                id,
                "My Task",
                "A task description",
                TaskStatusType.TODO,
                TaskPriorityType.MEDIUM,
                true,
                UUID.randomUUID().toString(),
                null
        );
    }
}
