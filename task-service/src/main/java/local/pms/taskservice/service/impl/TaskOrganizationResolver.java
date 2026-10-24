package local.pms.taskservice.service.impl;

import local.pms.taskservice.entity.Task;

import local.pms.taskservice.external.project.provider.ProjectOrganizationProvider;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;


import java.util.UUID;

/**
 * Resolves the organization of a task. {@code organizationId} is nullable on tasks persisted
 * before that column existed; for those rows it is looked up from project-service on each call.
 * Nothing is written back here: a write from inside the caller's (often read-only) transaction
 * either gets dropped or needs a second pooled connection per request.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TaskOrganizationResolver {

    private final ProjectOrganizationProvider projectOrganizationProvider;

    public UUID resolve(Task task) {
        if (task.getOrganizationId() != null) {
            return task.getOrganizationId();
        }
        log.debug("Resolving organizationId for legacy task '{}' from its project", task.getId());
        return projectOrganizationProvider.resolveOrganizationId(task.getProjectId());
    }
}
