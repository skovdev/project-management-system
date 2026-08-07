package local.pms.taskservice.service.impl;

import local.pms.taskservice.entity.Task;

import local.pms.taskservice.external.project.provider.ProjectOrganizationProvider;

import local.pms.taskservice.repository.TaskRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.InjectMocks;

import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TaskOrganizationResolverTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectOrganizationProvider projectOrganizationProvider;

    @InjectMocks
    private TaskOrganizationResolver taskOrganizationResolver;

    @Test
    @DisplayName("resolve returns the existing organizationId without calling project-service or saving")
    void should_returnExistingOrganizationId_when_alreadySet() {
        var organizationId = UUID.randomUUID();
        var task = buildTask(UUID.randomUUID(), organizationId);

        var result = taskOrganizationResolver.resolve(task);

        assertThat(result).isEqualTo(organizationId);
        verify(projectOrganizationProvider, never()).resolveOrganizationId(any());
        verify(taskRepository, never()).save(any());
    }

    @Test
    @DisplayName("resolve backfills organizationId from project-service and persists it when missing")
    void should_backfillAndSaveOrganizationId_when_missing() {
        var projectId = UUID.randomUUID();
        var organizationId = UUID.randomUUID();
        var task = buildTask(projectId, null);

        when(projectOrganizationProvider.resolveOrganizationId(projectId)).thenReturn(organizationId);

        var result = taskOrganizationResolver.resolve(task);

        assertThat(result).isEqualTo(organizationId);
        assertThat(task.getOrganizationId()).isEqualTo(organizationId);
        verify(taskRepository).save(task);
    }

    private Task buildTask(UUID projectId, UUID organizationId) {
        var task = new Task();
        task.setId(UUID.randomUUID());
        task.setProjectId(projectId);
        task.setOrganizationId(organizationId);
        return task;
    }
}
