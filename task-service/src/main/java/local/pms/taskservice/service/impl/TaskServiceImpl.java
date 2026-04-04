package local.pms.taskservice.service.impl;

import local.pms.taskservice.dto.TaskDto;

import local.pms.taskservice.exception.TaskNotFoundException;
import local.pms.taskservice.exception.InvalidTaskInputException;

import local.pms.taskservice.mapping.TaskMapping;

import local.pms.taskservice.repository.TaskRepository;

import local.pms.taskservice.service.TaskService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import lombok.experimental.FieldDefaults;

import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TaskServiceImpl implements TaskService {

    final TaskMapping taskMapping = TaskMapping.INSTANCE;

    final TaskRepository taskRepository;

    @Override
    @Transactional
    public TaskDto create(TaskDto taskDto) {
        if (taskDto == null) {
            log.error("TaskDto is null, cannot create task.");
            throw new InvalidTaskInputException("Task data cannot be null. Please provide valid task information");
        }
        var task = taskMapping.toEntity(taskDto);
        var savedTask = taskRepository.save(task);
        log.info("Task created with ID: {}", savedTask.getId());
        return taskMapping.toDto(savedTask);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TaskDto> findAll(Pageable pageable) {
        return taskRepository.findAll(pageable)
                .map(taskMapping::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskDto findById(UUID taskId) {
        var task = taskRepository.findById(taskId);
        if (task.isEmpty()) {
            log.error("Task with ID {} not found.", taskId);
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
        var existingTask = taskRepository.findById(taskId);
        if (existingTask.isEmpty()) {
            log.error("Task with ID {} not found, cannot update.", taskId);
            throw new TaskNotFoundException("Task with ID " + taskId + " not found. Please provide a valid task ID");
        }
        var taskToUpdate = existingTask.get();
        taskToUpdate.setTitle(taskDto.title());
        taskToUpdate.setDescription(taskDto.description());
        taskToUpdate.setTaskStatusType(taskDto.taskStatusType());
        taskToUpdate.setTaskPriorityType(taskDto.taskPriorityType());
        taskToUpdate.setActive(taskDto.active());
        taskToUpdate.setProjectId(UUID.fromString(taskDto.projectId()));
        taskToUpdate.setUserId(UUID.fromString(taskDto.userId()));
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
}