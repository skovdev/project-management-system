package local.pms.projectservice.kafka.consumer;

import local.pms.projectservice.event.OrganizationDeletedEvent;

import local.pms.projectservice.service.ProjectService;

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
class OrganizationDeletedConsumerTest {

    @Mock
    private ProjectService projectService;

    @InjectMocks
    private OrganizationDeletedConsumer consumer;

    @Test
    @DisplayName("onOrganizationDeleted calls deleteAllByOrganizationId with correct organizationId")
    void should_deleteAllProjects_when_onOrganizationDeletedReceived() {
        var organizationId = UUID.randomUUID();
        var event = new OrganizationDeletedEvent(organizationId);

        consumer.onOrganizationDeleted(event);

        verify(projectService).deleteAllByOrganizationId(organizationId);
    }

    @Test
    @DisplayName("onOrganizationDeleted rethrows exception so DefaultErrorHandler can route to DLT")
    void should_rethrowException_when_deleteAllByOrganizationIdFails() {
        var organizationId = UUID.randomUUID();
        var event = new OrganizationDeletedEvent(organizationId);

        doThrow(new RuntimeException("DB error")).when(projectService).deleteAllByOrganizationId(organizationId);

        assertThatThrownBy(() -> consumer.onOrganizationDeleted(event))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("DB error");

        verify(projectService).deleteAllByOrganizationId(organizationId);
    }
}
