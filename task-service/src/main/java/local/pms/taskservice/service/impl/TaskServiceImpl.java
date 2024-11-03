package local.pms.taskservice.service.impl;

import local.pms.taskservice.entity.Task;

import local.pms.taskservice.repository.TaskRepository;

import local.pms.taskservice.service.TaskService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import lombok.experimental.FieldDefaults;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TaskServiceImpl implements TaskService {

    final TaskRepository taskRepository;

    @Override
    public List<Task> findAll() {
        return taskRepository.findAll();
    }
}