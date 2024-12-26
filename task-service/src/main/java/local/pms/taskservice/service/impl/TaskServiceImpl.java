package local.pms.taskservice.service.impl;

import local.pms.taskservice.dto.TaskDto;

import local.pms.taskservice.mapping.TaskMapping;

import local.pms.taskservice.repository.TaskRepository;

import local.pms.taskservice.service.TaskService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import lombok.experimental.FieldDefaults;

import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TaskServiceImpl implements TaskService {

    final TaskMapping taskMapping = TaskMapping.INSTANCE;

    final TaskRepository taskRepository;

    @Override
    public Page<TaskDto> findAll(int page, int size, String sortBy, String order) {
        return taskRepository.findAll(pageRequest(page, size, sortBy, order))
                .map(taskMapping::toDto);
    }

    private PageRequest pageRequest(int page, int size, String sortBy, String order) {
        return PageRequest.of(page, size, sorting(sortBy, order));
    }

    private Sort sorting(String sortBy, String order) {
        return Sort.by(Sort.Order.by(sortBy)
                .with(Sort.Direction.fromString(order)));
    }
}