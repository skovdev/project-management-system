package local.pms.projectservice.listener;

import local.pms.projectservice.constant.KafkaConstants;

import local.pms.projectservice.event.ProjectCreatedEvent;

import local.pms.projectservice.kafka.producer.ProjectCreatedProducer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.InjectMocks;

import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProjectCreatedListenerTest {

    @Mock
    private ProjectCreatedProducer projectCreatedProducer;

    @InjectMocks
    private ProjectCreatedListener listener;

    @Test
    @DisplayName("handleProjectCreatedEvent delegates to the producer with the correct topic")
    void should_delegateToProducer_when_eventReceived() {
        var event = new ProjectCreatedEvent(UUID.randomUUID(), UUID.randomUUID(), "My Project");

        listener.handleProjectCreatedEvent(event);

        verify(projectCreatedProducer).sendProjectCreatedEvent(KafkaConstants.Topics.PROJECT_CREATED_TOPIC, event);
    }
}
