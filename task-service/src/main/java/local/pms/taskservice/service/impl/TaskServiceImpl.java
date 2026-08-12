package local.pms.taskservice.service.impl;

import local.pms.taskservice.config.jwt.JwtTokenProvider;

import local.pms.taskservice.dto.TaskDto;

import local.pms.taskservice.event.TaskCreatedEvent;

import local.pms.taskservice.exception.TaskNotFoundException;
import local.pms.taskservice.exception.InvalidTaskInputException;
import local.pms.taskservice.exception.TaskAccessDeniedException;
import local.pms.taskservice.exception.AcceptanceCriteriaGenerationException;

import local.pms.taskservice.entity.Task;

import local.pms.taskservice.external.ai.provider.AiExternalProvider;

import local.pms.taskservice.external.organization.provider.OrganizationAccessProvider;

import local.pms.taskservice.external.project.provider.ProjectOrganizationProvider;

import local.pms.taskservice.mapping.TaskMapping;

import local.pms.taskservice.repository.TaskRepository;

import local.pms.taskservice.service.TaskService;
import local.pms.taskservice.service.TokenService;

import local.pms.taskservice.type.OrganizationRoleType;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.context.ApplicationEventPublisher;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskMapping taskMapping = TaskMapping.INSTANCE;

    private final TaskRepository taskRepository;
    private final TokenService tokenService;
    private final JwtTokenProvider jwtTokenProvider;
    private final ApplicationEventPublisher eventPublisher;
    private final AiExternalProvider aiExternalProvider;
    private final OrganizationAccessProvider organizationAccessProvider;
    private final ProjectOrganizationProvider projectOrganizationProvider;
    private final TaskOrganizationResolver taskOrganizationResolver;

    @Override
    @Transactional
    public TaskDto create(TaskDto taskDto) {
        if (taskDto == null) {
            log.error("TaskDto is null, cannot create task.");
            throw new InvalidTaskInputException("Task data cannot be null. Please provide valid task information");
        }
        if (taskDto.projectId() == null || taskDto.projectId().isBlank()) {
            log.error("TaskDto projectId is null or blank, cannot create task.");
            throw new InvalidTaskInputException("Project ID cannot be null or blank. Please provide a valid project ID");
        }
        var projectId = UUID.fromString(taskDto.projectId());
        // project-service's organization-id lookup already verifies the caller is a member;
        // re-checking here would be a redundant second round trip for the same decision.
        var organizationId = projectOrganizationProvider.resolveOrganizationId(projectId);

        var task = taskMapping.toEntity(taskDto);
        task.setUserId(extractAuthUserId());
        task.setOrganizationId(organizationId);
        var savedTask = taskRepository.save(task);
        log.info("Task created with ID: {} in organization: {}", savedTask.getId(), savedTask.getOrganizationId());
        eventPublisher.publishEvent(
                new TaskCreatedEvent(savedTask.getId(), savedTask.getUserId(), savedTask.getTitle()));
        return taskMapping.toDto(savedTask);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TaskDto> findAll(Pageable pageable) {
        return taskRepository.findAllByUserId(extractAuthUserId(), pageable)
                .map(taskMapping::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TaskDto> findAllByProject(UUID projectId, Pageable pageable) {
        // project-service's organization-id lookup already verifies the caller is a member.
        projectOrganizationProvider.resolveOrganizationId(projectId);
        return taskRepository.findAllByProjectId(projectId, pageable)
                .map(taskMapping::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskDto findById(UUID taskId) {
        var task = findTaskOrThrow(taskId);
        organizationAccessProvider.verifyMembership(taskOrganizationResolver.resolve(task));
        return taskMapping.toDto(task);
    }

    @Override
    @Transactional
    public TaskDto update(UUID taskId, TaskDto taskDto) {
        if (taskDto == null) {
            log.error("TaskDto is null, cannot update task.");
            throw new InvalidTaskInputException("Task data cannot be null. Please provide valid task information");
        }
        if (taskDto.projectId() == null || taskDto.projectId().isBlank()) {
            log.error("TaskDto projectId is null or blank, cannot update task.");
            throw new InvalidTaskInputException("Project ID cannot be null or blank. Please provide a valid project ID");
        }
        UUID authUserId = extractAuthUserId();
        var taskToUpdate = findTaskOrThrow(taskId);
        requireEditAccess(taskToUpdate, authUserId);

        var newProjectId = UUID.fromString(taskDto.projectId());
        if (!newProjectId.equals(taskToUpdate.getProjectId())) {
            // project-service's organization-id lookup already verifies the caller is a
            // member of the new project's organization.
            var newOrganizationId = projectOrganizationProvider.resolveOrganizationId(newProjectId);
            taskToUpdate.setOrganizationId(newOrganizationId);
        }
        taskToUpdate.setTitle(taskDto.title());
        taskToUpdate.setDescription(taskDto.description());
        taskToUpdate.setTaskStatusType(taskDto.taskStatusType());
        taskToUpdate.setTaskPriorityType(taskDto.taskPriorityType());
        taskToUpdate.setActive(taskDto.active());
        taskToUpdate.setProjectId(newProjectId);
        taskToUpdate.setAcceptanceCriteria(taskDto.acceptanceCriteria());
        var updatedTask = taskRepository.save(taskToUpdate);
        log.info("Task with ID {} updated successfully.", taskId);
        return taskMapping.toDto(updatedTask);
    }

    @Override
    @Transactional
    public void delete(UUID taskId) {
        UUID authUserId = extractAuthUserId();
        var task = findTaskOrThrow(taskId);
        requireEditAccess(task, authUserId);
        taskRepository.deleteById(taskId);
        log.info("Task with ID {} deleted successfully.", taskId);
    }

    @Override
    @Transactional
    public void deleteAllByProjectId(UUID projectId) {
        log.info("Deleting all tasks for projectId: {}", projectId);
        taskRepository.deleteAllByProjectId(projectId);
        log.info("All tasks deleted for projectId: {}", projectId);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public String generateAcceptanceCriteria(UUID taskId) {
        var task = findTaskOrThrow(taskId);
        organizationAccessProvider.verifyMembership(taskOrganizationResolver.resolve(task));
        try {
            log.info("Generating acceptance criteria for taskId: {}", taskId);
            return aiExternalProvider.generateAcceptanceCriteria(task.getTitle(), task.getDescription());
        } catch (Exception e) {
            log.error("Failed to generate acceptance criteria for taskId '{}': {}", taskId, e.getMessage());
            throw new AcceptanceCriteriaGenerationException(
                    "An error occurred while generating acceptance criteria", e);
        }
    }

    private Task findTaskOrThrow(UUID taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> {
                    log.error("Task with ID {} not found.", taskId);
                    return new TaskNotFoundException("Task with ID " + taskId + " not found. Please provide a valid task ID");
                });
    }

    private void requireEditAccess(Task task, UUID authUserId) {
        var role = organizationAccessProvider.verifyMembership(taskOrganizationResolver.resolve(task));
        boolean isCreator = task.getUserId().equals(authUserId);
        boolean isOrgManager = role.isAtLeast(OrganizationRoleType.ADMIN);
        if (!isCreator && !isOrgManager) {
            log.error("User {} with role {} attempted a restricted action on task {}.", authUserId, role, task.getId());
            throw new TaskAccessDeniedException("Access denied: you do not have sufficient permissions for task with ID " + task.getId());
        }
    }

    private UUID extractAuthUserId() {
        if (tokenService.getToken() == null || tokenService.getToken().isBlank()) {
            log.error("JWT token is missing or blank, cannot extract authenticated user ID.");
            throw new TaskAccessDeniedException("Access denied: missing or invalid authentication token");
        }
        return jwtTokenProvider.extractAuthUserId(tokenService.getToken());
    }
}