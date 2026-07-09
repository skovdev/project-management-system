package local.pms.taskservice.listener;

import local.pms.taskservice.constant.KafkaConstants;

import local.pms.taskservice.event.TaskCreatedEvent;

import local.pms.taskservice.kafka.producer.TaskCreatedProducer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.InjectMocks;

import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TaskCreatedListenerTest {

    @Mock
    private TaskCreatedProducer taskCreatedProducer;

    @InjectMocks
    private TaskCreatedListener listener;

    @Test
    @DisplayName("handleTaskCreatedEvent delegates to the producer with the correct topic")
    void should_delegateToProducer_when_eventReceived() {
        var event = new TaskCreatedEvent(UUID.randomUUID(), UUID.randomUUID(), "My Task");

        listener.handleTaskCreatedEvent(event);

        verify(taskCreatedProducer).sendTaskCreatedEvent(KafkaConstants.Topics.TASK_CREATED_TOPIC, event);
    }
}
