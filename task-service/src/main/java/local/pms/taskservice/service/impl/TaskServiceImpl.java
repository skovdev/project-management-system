package local.pms.taskservice.service.impl;

import local.pms.taskservice.config.jwt.JwtTokenProvider;

import local.pms.taskservice.dto.TaskDto;

import local.pms.taskservice.exception.TaskNotFoundException;
import local.pms.taskservice.exception.InvalidTaskInputException;
import local.pms.taskservice.exception.TaskAccessDeniedException;

import local.pms.taskservice.mapping.TaskMapping;

import local.pms.taskservice.repository.TaskRepository;

import local.pms.taskservice.service.TaskService;
import local.pms.taskservice.service.TokenService;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

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

    @Override
    @Transactional
    public TaskDto create(TaskDto taskDto) {
        if (taskDto == null) {
            log.error("TaskDto is null, cannot create task.");
            throw new InvalidTaskInputException("Task data cannot be null. Please provide valid task information");
        }
        var task = taskMapping.toEntity(taskDto);
        task.setUserId(extractAuthUserId());
        var savedTask = taskRepository.save(task);
        log.info("Task created with ID: {}", savedTask.getId());
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
    public TaskDto findById(UUID taskId) {
        var task = taskRepository.findByIdAndUserId(taskId, extractAuthUserId());
        if (task.isEmpty()) {
            log.error("Task with ID {} not found or access denied.", taskId);
            throw new TaskNotFoundException("Task with ID " + taskId + " not found. Please provide a valid task ID");
        }
        return taskMapping.toDto(task.get());
    }

    @Override
    @Transactional
    public TaskDto update(UUID taskId, TaskDto taskDto) {
        if (taskDto == null) {
            log.error("TaskDto is null, cannot update task.");
            throw new InvalidTaskInputException("Task data cannot be null. Please provide valid task information");
        }
        UUID authUserId = extractAuthUserId();
        var existingTask = taskRepository.findById(taskId);
        if (existingTask.isEmpty()) {
            log.error("Task with ID {} not found, cannot update.", taskId);
            throw new TaskNotFoundException("Task with ID " + taskId + " not found. Please provide a valid task ID");
        }
        if (!existingTask.get().getUserId().equals(authUserId)) {
            log.error("User {} attempted to update task {} owned by another user.", authUserId, taskId);
            throw new TaskAccessDeniedException("Access denied: you do not own task with ID " + taskId);
        }
        var taskToUpdate = existingTask.get();
        taskToUpdate.setTitle(taskDto.title());
        taskToUpdate.setDescription(taskDto.description());
        taskToUpdate.setTaskStatusType(taskDto.taskStatusType());
        taskToUpdate.setTaskPriorityType(taskDto.taskPriorityType());
        taskToUpdate.setActive(taskDto.active());
        if (taskDto.projectId() == null || taskDto.projectId().isBlank()) {
            log.error("TaskDto projectId is null or blank, cannot update task.");
            throw new InvalidTaskInputException("Project ID cannot be null or blank. Please provide a valid project ID");
        }
        taskToUpdate.setProjectId(UUID.fromString(taskDto.projectId()));
        var updatedTask = taskRepository.save(taskToUpdate);
        log.info("Task with ID {} updated successfully.", taskId);
        return taskMapping.toDto(updatedTask);
    }

    @Override
    @Transactional
    public void delete(UUID taskId) {
        var task = taskRepository.findById(taskId);
        if (task.isEmpty()) {
            log.error("Task with ID {} not found, cannot delete.", taskId);
            throw new TaskNotFoundException("Task with ID " + taskId + " not found. Please provide a valid task ID");
        }
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

    private UUID extractAuthUserId() {
        if (tokenService.getToken() == null || tokenService.getToken().isBlank()) {
            log.error("JWT token is missing or blank, cannot extract authenticated user ID.");
            throw new TaskAccessDeniedException("Access denied: missing or invalid authentication token");
        }
        return jwtTokenProvider.extractAuthUserId(tokenService.getToken());
    }
}