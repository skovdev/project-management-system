package local.pms.taskservice.kafka.consumer;

import local.pms.taskservice.event.ProjectDeletedEvent;

import local.pms.taskservice.service.TaskService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.InjectMocks;

import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
class ProjectDeletedConsumerTest {

    @Mock
    private TaskService taskService;

    @InjectMocks
    private ProjectDeletedConsumer consumer;

    @Test
    @DisplayName("onProjectDeleted calls deleteAllByProjectId with correct projectId")
    void should_deleteAllTasks_when_onProjectDeletedReceived() {
        var projectId = UUID.randomUUID();
        var event = new ProjectDeletedEvent(projectId);

        consumer.onProjectDeleted(event);

        verify(taskService).deleteAllByProjectId(projectId);
    }

    @Test
    @DisplayName("onProjectDeleted rethrows exception so DefaultErrorHandler can route to DLT")
    void should_rethrowException_when_deleteAllByProjectIdFails() {
        var projectId = UUID.randomUUID();
        var event = new ProjectDeletedEvent(projectId);

        doThrow(new RuntimeException("DB error")).when(taskService).deleteAllByProjectId(projectId);

        assertThatThrownBy(() -> consumer.onProjectDeleted(event))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("DB error");

        verify(taskService).deleteAllByProjectId(projectId);
    }
}
