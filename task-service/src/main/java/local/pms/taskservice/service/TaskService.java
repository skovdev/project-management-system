package local.pms.taskservice.service;

import local.pms.taskservice.dto.TaskDto;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Service interface for task management operations.
 */
public interface TaskService {

    /**
     * Creates a new task.
     *
     * @param taskDto the task data to create
     * @return the created task DTO
     */
    TaskDto create(TaskDto taskDto);

    /**
     * Retrieves a paginated list of all tasks.
     *
     * @param pageable pagination and sorting parameters
     * @return a page of task DTOs
     */
    Page<TaskDto> findAll(Pageable pageable);

    /**
     * Finds a task by its identifier.
     *
     * @param taskId the unique task identifier
     * @return the task DTO
     */
    TaskDto findById(UUID taskId);

    /**
     * Updates an existing task with new data.
     *
     * @param taskId  the unique task identifier
     * @param taskDto the updated task data
     * @return the updated task DTO
     */
    TaskDto update(UUID taskId, TaskDto taskDto);

    /**
     * Soft-deletes a task by its identifier.
     *
     * @param taskId the unique task identifier
     */
    void delete(UUID taskId);
}
