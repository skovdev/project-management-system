package local.pms.taskservice.service;

import local.pms.taskservice.dto.TaskDto;

import org.springframework.data.domain.Page;

public interface TaskService {
    Page<TaskDto> findAll(int page, int size, String sortBy, String order);
}
