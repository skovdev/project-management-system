package local.pms.projectservice.listener;

import local.pms.projectservice.constant.KafkaConstants;

import local.pms.projectservice.event.ProjectDeletedEvent;

import local.pms.projectservice.kafka.producer.ProjectDeletedProducer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.InjectMocks;

import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProjectDeletedListenerTest {

    @Mock
    private ProjectDeletedProducer projectDeletedProducer;

    @InjectMocks
    private ProjectDeletedListener listener;

    @Test
    @DisplayName("handleProjectDeletedEvent delegates to the producer with the correct topic")
    void should_delegateToProducer_when_eventReceived() {
        var event = new ProjectDeletedEvent(UUID.randomUUID());

        listener.handleProjectDeletedEvent(event);

        verify(projectDeletedProducer).sendProjectDeletedEvent(KafkaConstants.Topics.PROJECT_DELETED_TOPIC, event);
    }
}
