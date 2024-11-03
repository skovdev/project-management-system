package local.pms.taskservice.service;

import local.pms.taskservice.entity.Task;

import java.util.List;

public interface TaskService {
    List<Task> findAll();
}
