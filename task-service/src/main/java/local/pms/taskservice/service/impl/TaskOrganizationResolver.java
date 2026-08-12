package local.pms.taskservice.service.impl;

import local.pms.taskservice.entity.Task;

import local.pms.taskservice.external.project.provider.ProjectOrganizationProvider;

import local.pms.taskservice.repository.TaskRepository;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Backfills {@code organizationId} for tasks persisted before that column existed.
 * The column is nullable at the DB level for exactly this reason; every read path
 * that needs a task's organization for a membership check should go through here
 * so legacy rows self-heal instead of failing with a null organization ID.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TaskOrganizationResolver {

    private final TaskRepository taskRepository;
    private final ProjectOrganizationProvider projectOrganizationProvider;

    /**
     * Runs in its own transaction: most call sites resolve a task's organization from
     * inside a {@code readOnly} transaction, which would otherwise silently drop (or
     * reject) the backfill write below.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public UUID resolve(Task task) {
        if (task.getOrganizationId() != null) {
            return task.getOrganizationId();
        }
        log.info("Backfilling organizationId for legacy task '{}'", task.getId());
        var organizationId = projectOrganizationProvider.resolveOrganizationId(task.getProjectId());
        task.setOrganizationId(organizationId);
        taskRepository.save(task);
        return organizationId;
    }
}
