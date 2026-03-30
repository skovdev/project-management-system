package local.pms.taskservice.service.impl;

import local.pms.taskservice.dto.TaskDto;

import local.pms.taskservice.entity.Task;

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
        Task task = taskMapping.toEntity(taskDto);
        Task savedTask = taskRepository.save(task);
        log.info("Task created with ID: {}", savedTask.getId());
        return taskMapping.toDto(savedTask);
    }

    @Override
    public Page<TaskDto> findAll(Pageable pageable) {
        return taskRepository.findAll(pageable)
                .map(taskMapping::toDto);
    }
}