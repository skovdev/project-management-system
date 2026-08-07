package local.pms.taskservice.service;

import local.pms.taskservice.config.jwt.JwtTokenProvider;

import local.pms.taskservice.dto.TaskDto;

import local.pms.taskservice.entity.Task;

import local.pms.taskservice.event.TaskCreatedEvent;

import local.pms.taskservice.exception.TaskNotFoundException;
import local.pms.taskservice.exception.TaskAccessDeniedException;
import local.pms.taskservice.exception.InvalidTaskInputException;
import local.pms.taskservice.exception.AcceptanceCriteriaGenerationException;

import local.pms.taskservice.external.ai.provider.AiExternalProvider;

import local.pms.taskservice.external.organization.provider.OrganizationAccessProvider;

import local.pms.taskservice.external.project.provider.ProjectOrganizationProvider;

import local.pms.taskservice.repository.TaskRepository;

import local.pms.taskservice.service.impl.TaskServiceImpl;
import local.pms.taskservice.service.impl.TaskOrganizationResolver;

import local.pms.taskservice.type.TaskStatusType;
import local.pms.taskservice.type.TaskPriorityType;
import local.pms.taskservice.type.OrganizationRoleType;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.InjectMocks;

import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.context.ApplicationEventPublisher;

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
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TokenService tokenService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private AiExternalProvider aiExternalProvider;

    @Mock
    private OrganizationAccessProvider organizationAccessProvider;

    @Mock
    private ProjectOrganizationProvider projectOrganizationProvider;

    @Mock
    private TaskOrganizationResolver taskOrganizationResolver;

    @InjectMocks
    private TaskServiceImpl taskService;

    @BeforeEach
    void setUp() {
        lenient().when(taskOrganizationResolver.resolve(any(Task.class)))
                .thenAnswer(invocation -> ((Task) invocation.getArgument(0)).getOrganizationId());
    }

    @Test
    @DisplayName("create saves task and returns DTO with userId and organizationId resolved")
    void should_saveAndReturnDto_when_createWithValidData() {
        var userId = UUID.randomUUID();
        var projectId = UUID.randomUUID();
        var organizationId = UUID.randomUUID();
        var dto = buildTaskDto(null, projectId);
        var saved = buildTask(UUID.randomUUID(), userId, projectId, organizationId);

        stubToken(userId);
        when(projectOrganizationProvider.resolveOrganizationId(projectId)).thenReturn(organizationId);
        when(taskRepository.save(any(Task.class))).thenReturn(saved);

        var result = taskService.create(dto);

        assertThat(result.title()).isEqualTo("My Task");
        verify(taskRepository).save(any(Task.class));
        verify(eventPublisher).publishEvent(any(TaskCreatedEvent.class));
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
    @DisplayName("create throws TaskAccessDeniedException when caller is not a member of the project's organization")
    void should_throwTaskAccessDeniedException_when_createByNonMember() {
        var projectId = UUID.randomUUID();

        when(projectOrganizationProvider.resolveOrganizationId(projectId))
                .thenThrow(new TaskAccessDeniedException("Access denied: unable to resolve organization for project with ID " + projectId));

        assertThatThrownBy(() -> taskService.create(buildTaskDto(null, projectId)))
                .isInstanceOf(TaskAccessDeniedException.class);

        verify(taskRepository, never()).save(any());
    }

    @Test
    @DisplayName("create throws InvalidTaskInputException when projectId is blank")
    void should_throwInvalidTaskInputException_when_createWithBlankProjectId() {
        var badDto = new TaskDto(null, "My Task", "A task description",
                TaskStatusType.TODO, TaskPriorityType.MEDIUM, true, "  ", null, null, null);

        assertThatThrownBy(() -> taskService.create(badDto))
                .isInstanceOf(InvalidTaskInputException.class)
                .hasMessageContaining("Project ID cannot be null or blank");

        verify(taskRepository, never()).save(any());
    }

    @Test
    @DisplayName("findAll returns page of DTOs filtered by userId from token")
    void should_returnPageOfDtos_when_findAll() {
        var userId = UUID.randomUUID();
        var pageable = PageRequest.of(0, 10);
        var task = buildTask(UUID.randomUUID(), userId, UUID.randomUUID(), UUID.randomUUID());
        var page = new PageImpl<>(List.of(task), pageable, 1);

        stubToken(userId);
        when(taskRepository.findAllByUserId(userId, pageable)).thenReturn(page);

        var result = taskService.findAll(pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).title()).isEqualTo("My Task");
    }

    @Test
    @DisplayName("findAllByProject returns page of DTOs when caller is a member of the project's organization")
    void should_returnPageOfDtos_when_findAllByProject() {
        var projectId = UUID.randomUUID();
        var organizationId = UUID.randomUUID();
        var pageable = PageRequest.of(0, 10);
        var task = buildTask(UUID.randomUUID(), UUID.randomUUID(), projectId, organizationId);
        var page = new PageImpl<>(List.of(task), pageable, 1);

        when(projectOrganizationProvider.resolveOrganizationId(projectId)).thenReturn(organizationId);
        when(taskRepository.findAllByProjectId(projectId, pageable)).thenReturn(page);

        var result = taskService.findAllByProject(projectId, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("findAllByProject throws TaskAccessDeniedException when caller is not a member of the project's organization")
    void should_throwTaskAccessDeniedException_when_findAllByProjectByNonMember() {
        var projectId = UUID.randomUUID();
        var pageable = PageRequest.of(0, 10);

        when(projectOrganizationProvider.resolveOrganizationId(projectId))
                .thenThrow(new TaskAccessDeniedException("Access denied: unable to resolve organization for project with ID " + projectId));

        assertThatThrownBy(() -> taskService.findAllByProject(projectId, pageable))
                .isInstanceOf(TaskAccessDeniedException.class);

        verify(taskRepository, never()).findAllByProjectId(any(), any());
    }

    @Test
    @DisplayName("findById returns DTO when caller is a member of the task's organization")
    void should_returnDto_when_findByIdExists() {
        var taskId = UUID.randomUUID();
        var organizationId = UUID.randomUUID();
        var task = buildTask(taskId, UUID.randomUUID(), UUID.randomUUID(), organizationId);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(organizationAccessProvider.verifyMembership(organizationId)).thenReturn(OrganizationRoleType.MEMBER);

        var result = taskService.findById(taskId);

        assertThat(result.id()).isEqualTo(taskId.toString());
        assertThat(result.title()).isEqualTo("My Task");
    }

    @Test
    @DisplayName("findById throws TaskNotFoundException when task not found")
    void should_throwTaskNotFoundException_when_findByIdNotFound() {
        var taskId = UUID.randomUUID();

        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.findById(taskId))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessageContaining(taskId.toString());
    }

    @Test
    @DisplayName("findById throws TaskAccessDeniedException when caller is not a member of the task's organization")
    void should_throwTaskAccessDeniedException_when_findByIdByNonMember() {
        var taskId = UUID.randomUUID();
        var organizationId = UUID.randomUUID();
        var task = buildTask(taskId, UUID.randomUUID(), UUID.randomUUID(), organizationId);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(organizationAccessProvider.verifyMembership(organizationId))
                .thenThrow(new TaskAccessDeniedException("Access denied"));

        assertThatThrownBy(() -> taskService.findById(taskId))
                .isInstanceOf(TaskAccessDeniedException.class);
    }

    @Test
    @DisplayName("update returns updated DTO when caller owns the task")
    void should_returnUpdatedDto_when_callerOwnsTask() {
        var taskId = UUID.randomUUID();
        var userId = UUID.randomUUID();
        var projectId = UUID.randomUUID();
        var organizationId = UUID.randomUUID();
        var existing = buildTask(taskId, userId, projectId, organizationId);
        var updateDto = buildTaskDto(taskId.toString(), projectId);

        stubToken(userId);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existing));
        when(organizationAccessProvider.verifyMembership(organizationId)).thenReturn(OrganizationRoleType.MEMBER);
        when(taskRepository.save(existing)).thenReturn(existing);

        var result = taskService.update(taskId, updateDto);

        assertThat(result).isNotNull();
        verify(taskRepository).save(existing);
    }

    @Test
    @DisplayName("update resolves the new organization via project-service without a second membership check when the project changes")
    void should_reassignOrganization_when_projectChangesOnUpdate() {
        var taskId = UUID.randomUUID();
        var userId = UUID.randomUUID();
        var oldProjectId = UUID.randomUUID();
        var newProjectId = UUID.randomUUID();
        var oldOrganizationId = UUID.randomUUID();
        var newOrganizationId = UUID.randomUUID();
        var existing = buildTask(taskId, userId, oldProjectId, oldOrganizationId);
        var updateDto = buildTaskDto(taskId.toString(), newProjectId);

        stubToken(userId);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existing));
        when(organizationAccessProvider.verifyMembership(oldOrganizationId)).thenReturn(OrganizationRoleType.MEMBER);
        when(projectOrganizationProvider.resolveOrganizationId(newProjectId)).thenReturn(newOrganizationId);
        when(taskRepository.save(existing)).thenReturn(existing);

        var result = taskService.update(taskId, updateDto);

        assertThat(result).isNotNull();
        assertThat(existing.getOrganizationId()).isEqualTo(newOrganizationId);
        verify(organizationAccessProvider, never()).verifyMembership(newOrganizationId);
        verify(taskRepository).save(existing);
    }

    @Test
    @DisplayName("update returns updated DTO when caller is an OWNER/ADMIN of the organization even if not the creator")
    void should_returnUpdatedDto_when_callerIsOrgManager() {
        var taskId = UUID.randomUUID();
        var ownerId = UUID.randomUUID();
        var callerId = UUID.randomUUID();
        var projectId = UUID.randomUUID();
        var organizationId = UUID.randomUUID();
        var existing = buildTask(taskId, ownerId, projectId, organizationId);
        var updateDto = buildTaskDto(taskId.toString(), projectId);

        stubToken(callerId);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existing));
        when(organizationAccessProvider.verifyMembership(organizationId)).thenReturn(OrganizationRoleType.ADMIN);
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

        stubToken(UUID.randomUUID());
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.update(taskId, buildTaskDto(taskId.toString(), UUID.randomUUID())))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessageContaining(taskId.toString());

        verify(taskRepository, never()).save(any());
    }

    @Test
    @DisplayName("update throws TaskAccessDeniedException when caller neither owns the task nor manages the organization")
    void should_throwTaskAccessDeniedException_when_callerNotOwnerNorManager() {
        var taskId = UUID.randomUUID();
        var ownerId = UUID.randomUUID();
        var callerId = UUID.randomUUID();
        var projectId = UUID.randomUUID();
        var organizationId = UUID.randomUUID();
        var existing = buildTask(taskId, ownerId, projectId, organizationId);

        stubToken(callerId);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existing));
        when(organizationAccessProvider.verifyMembership(organizationId)).thenReturn(OrganizationRoleType.MEMBER);

        assertThatThrownBy(() -> taskService.update(taskId, buildTaskDto(taskId.toString(), projectId)))
                .isInstanceOf(TaskAccessDeniedException.class)
                .hasMessageContaining(taskId.toString());

        verify(taskRepository, never()).save(any());
    }

    @Test
    @DisplayName("update throws InvalidTaskInputException when projectId is blank")
    void should_throwInvalidTaskInputException_when_projectIdIsBlank() {
        var taskId = UUID.randomUUID();
        var badDto = new TaskDto(taskId.toString(), "My Task", "A task description",
                TaskStatusType.TODO, TaskPriorityType.MEDIUM, true, "  ", null, null, null);

        assertThatThrownBy(() -> taskService.update(taskId, badDto))
                .isInstanceOf(InvalidTaskInputException.class)
                .hasMessageContaining("Project ID cannot be null or blank");

        verify(taskRepository, never()).save(any());
    }

    @Test
    @DisplayName("delete removes task when caller owns the task")
    void should_deleteTask_when_callerOwnsTask() {
        var taskId = UUID.randomUUID();
        var userId = UUID.randomUUID();
        var organizationId = UUID.randomUUID();
        var task = buildTask(taskId, userId, UUID.randomUUID(), organizationId);

        stubToken(userId);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(organizationAccessProvider.verifyMembership(organizationId)).thenReturn(OrganizationRoleType.MEMBER);

        taskService.delete(taskId);

        verify(taskRepository).deleteById(taskId);
    }

    @Test
    @DisplayName("delete throws TaskAccessDeniedException when caller neither owns the task nor manages the organization")
    void should_throwTaskAccessDeniedException_when_deleteByNonOwnerNonManager() {
        var taskId = UUID.randomUUID();
        var ownerId = UUID.randomUUID();
        var callerId = UUID.randomUUID();
        var organizationId = UUID.randomUUID();
        var task = buildTask(taskId, ownerId, UUID.randomUUID(), organizationId);

        stubToken(callerId);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(organizationAccessProvider.verifyMembership(organizationId)).thenReturn(OrganizationRoleType.MEMBER);

        assertThatThrownBy(() -> taskService.delete(taskId))
                .isInstanceOf(TaskAccessDeniedException.class)
                .hasMessageContaining(taskId.toString());

        verify(taskRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("delete throws TaskNotFoundException when task not found")
    void should_throwTaskNotFoundException_when_deleteNotFound() {
        var taskId = UUID.randomUUID();
        stubToken(UUID.randomUUID());
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

    @Test
    @DisplayName("generateAcceptanceCriteria returns AI-generated text when caller is a member of the task's organization")
    void should_returnGeneratedText_when_generateAcceptanceCriteriaByMember() {
        var taskId = UUID.randomUUID();
        var organizationId = UUID.randomUUID();
        var task = buildTask(taskId, UUID.randomUUID(), UUID.randomUUID(), organizationId);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(organizationAccessProvider.verifyMembership(organizationId)).thenReturn(OrganizationRoleType.MEMBER);
        when(aiExternalProvider.generateAcceptanceCriteria("My Task", "A task description"))
                .thenReturn("Given ... When ... Then ...");

        var result = taskService.generateAcceptanceCriteria(taskId);

        assertThat(result).isEqualTo("Given ... When ... Then ...");
        verify(aiExternalProvider).generateAcceptanceCriteria("My Task", "A task description");
    }

    @Test
    @DisplayName("generateAcceptanceCriteria throws TaskNotFoundException when task not found")
    void should_throwTaskNotFoundException_when_generateAcceptanceCriteriaTaskNotFound() {
        var taskId = UUID.randomUUID();

        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.generateAcceptanceCriteria(taskId))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessageContaining(taskId.toString());

        verify(aiExternalProvider, never()).generateAcceptanceCriteria(any(), any());
    }

    @Test
    @DisplayName("generateAcceptanceCriteria wraps AI exception in AcceptanceCriteriaGenerationException")
    void should_throwAcceptanceCriteriaGenerationException_when_aiProviderFails() {
        var taskId = UUID.randomUUID();
        var organizationId = UUID.randomUUID();
        var task = buildTask(taskId, UUID.randomUUID(), UUID.randomUUID(), organizationId);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(organizationAccessProvider.verifyMembership(organizationId)).thenReturn(OrganizationRoleType.MEMBER);
        when(aiExternalProvider.generateAcceptanceCriteria(any(), any()))
                .thenThrow(new RuntimeException("AI unavailable"));

        assertThatThrownBy(() -> taskService.generateAcceptanceCriteria(taskId))
                .isInstanceOf(AcceptanceCriteriaGenerationException.class)
                .hasMessageContaining("error occurred while generating acceptance criteria");
    }

    private void stubToken(UUID userId) {
        lenient().when(tokenService.getToken()).thenReturn("test-token");
        lenient().when(jwtTokenProvider.extractAuthUserId("test-token")).thenReturn(userId);
    }

    private Task buildTask(UUID id, UUID userId, UUID projectId, UUID organizationId) {
        var task = new Task();
        task.setId(id);
        task.setTitle("My Task");
        task.setDescription("A task description");
        task.setTaskStatusType(TaskStatusType.TODO);
        task.setTaskPriorityType(TaskPriorityType.MEDIUM);
        task.setActive(true);
        task.setProjectId(projectId);
        task.setUserId(userId);
        task.setOrganizationId(organizationId);
        task.setDeleted(false);
        return task;
    }

    private TaskDto buildTaskDto(String id, UUID projectId) {
        return new TaskDto(
                id,
                "My Task",
                "A task description",
                TaskStatusType.TODO,
                TaskPriorityType.MEDIUM,
                true,
                projectId.toString(),
                null,
                null,
                null
        );
    }
}
