package local.pms.projectservice.kafka.producer;

import local.pms.projectservice.constant.KafkaConstants;

import local.pms.projectservice.event.ProjectDeletedEvent;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.ArgumentCaptor;

import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.kafka.core.KafkaTemplate;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class ProjectDeletedProducerTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private ProjectDeletedProducer producer;

    @Test
    @DisplayName("sendProjectDeletedEvent sends event to the project-deleted topic")
    void should_sendEvent_when_sendProjectDeletedEventCalled() {
        var projectId = UUID.randomUUID();
        var event = new ProjectDeletedEvent(projectId);
        var future = new org.springframework.kafka.support.SendResult<String, Object>(null, null);
        when(kafkaTemplate.send(any(String.class), any()))
                .thenReturn(java.util.concurrent.CompletableFuture.completedFuture(future));

        producer.sendProjectDeletedEvent(KafkaConstants.Topics.PROJECT_DELETED_TOPIC, event);

        var topicCaptor = ArgumentCaptor.forClass(String.class);
        var eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(kafkaTemplate).send(topicCaptor.capture(), eventCaptor.capture());

        assertThat(topicCaptor.getValue()).isEqualTo(KafkaConstants.Topics.PROJECT_DELETED_TOPIC);
        assertThat(eventCaptor.getValue()).isEqualTo(event);
    }

    @Test
    @DisplayName("sendProjectDeletedEvent sends correct projectId in event")
    void should_sendCorrectProjectId_when_sendProjectDeletedEventCalled() {
        var projectId = UUID.randomUUID();
        var event = new ProjectDeletedEvent(projectId);
        var future = new org.springframework.kafka.support.SendResult<String, Object>(null, null);
        when(kafkaTemplate.send(eq(KafkaConstants.Topics.PROJECT_DELETED_TOPIC), eq(event)))
                .thenReturn(java.util.concurrent.CompletableFuture.completedFuture(future));

        producer.sendProjectDeletedEvent(KafkaConstants.Topics.PROJECT_DELETED_TOPIC, event);

        verify(kafkaTemplate).send(KafkaConstants.Topics.PROJECT_DELETED_TOPIC, event);
    }
}
