package local.pms.taskservice.service;

import local.pms.taskservice.dto.TaskDto;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TaskService {
    Page<TaskDto> findAll(Pageable pageable);
}
