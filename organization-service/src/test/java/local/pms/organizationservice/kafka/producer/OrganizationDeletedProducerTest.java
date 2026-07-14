package local.pms.organizationservice.kafka.producer;

import ch.qos.logback.classic.Logger;

import ch.qos.logback.classic.spi.ILoggingEvent;

import ch.qos.logback.core.read.ListAppender;

import local.pms.organizationservice.constant.KafkaConstants;

import local.pms.organizationservice.event.OrganizationDeletedEvent;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.ArgumentCaptor;

import org.mockito.junit.jupiter.MockitoExtension;

import org.slf4j.LoggerFactory;

import org.springframework.kafka.core.KafkaTemplate;

import org.springframework.kafka.support.SendResult;

import java.util.UUID;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class OrganizationDeletedProducerTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private OrganizationDeletedProducer producer;

    private ListAppender<ILoggingEvent> logAppender;

    @BeforeEach
    void attachLogAppender() {
        logAppender = new ListAppender<>();
        logAppender.start();
        ((Logger) LoggerFactory.getLogger(OrganizationDeletedProducer.class)).addAppender(logAppender);
    }

    @AfterEach
    void detachLogAppender() {
        ((Logger) LoggerFactory.getLogger(OrganizationDeletedProducer.class)).detachAppender(logAppender);
    }

    @Test
    @DisplayName("sendOrganizationDeletedEvent sends event to the organization-deleted topic")
    void should_sendEvent_when_sendOrganizationDeletedEventCalled() {
        var organizationId = UUID.randomUUID();
        var event = new OrganizationDeletedEvent(organizationId);
        var future = new SendResult<String, Object>(null, null);
        when(kafkaTemplate.send(any(String.class), any()))
                .thenReturn(CompletableFuture.completedFuture(future));

        producer.sendOrganizationDeletedEvent(KafkaConstants.Topics.ORGANIZATION_DELETED_TOPIC, event);

        var topicCaptor = ArgumentCaptor.forClass(String.class);
        var eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(kafkaTemplate).send(topicCaptor.capture(), eventCaptor.capture());

        assertThat(topicCaptor.getValue()).isEqualTo(KafkaConstants.Topics.ORGANIZATION_DELETED_TOPIC);
        assertThat(eventCaptor.getValue()).isEqualTo(event);
    }

    @Test
    @DisplayName("sendOrganizationDeletedEvent sends correct organizationId in event")
    void should_sendCorrectOrganizationId_when_sendOrganizationDeletedEventCalled() {
        var organizationId = UUID.randomUUID();
        var event = new OrganizationDeletedEvent(organizationId);
        var future = new SendResult<String, Object>(null, null);
        when(kafkaTemplate.send(eq(KafkaConstants.Topics.ORGANIZATION_DELETED_TOPIC), eq(event)))
                .thenReturn(CompletableFuture.completedFuture(future));

        producer.sendOrganizationDeletedEvent(KafkaConstants.Topics.ORGANIZATION_DELETED_TOPIC, event);

        verify(kafkaTemplate).send(KafkaConstants.Topics.ORGANIZATION_DELETED_TOPIC, event);
    }

    @Test
    @DisplayName("sendOrganizationDeletedEvent logs the failure with the topic instead of crashing when the Kafka send fails")
    void should_logFailureWithTopic_when_kafkaSendFails() {
        var organizationId = UUID.randomUUID();
        var event = new OrganizationDeletedEvent(organizationId);
        var failedFuture = CompletableFuture.<SendResult<String, Object>>failedFuture(new RuntimeException("broker unavailable"));
        when(kafkaTemplate.send(any(String.class), any())).thenReturn(failedFuture);

        producer.sendOrganizationDeletedEvent(KafkaConstants.Topics.ORGANIZATION_DELETED_TOPIC, event);

        assertThat(logAppender.list)
                .anySatisfy(loggingEvent -> {
                    assertThat(loggingEvent.getFormattedMessage())
                            .contains("Failed to publish organization-deleted event")
                            .contains(KafkaConstants.Topics.ORGANIZATION_DELETED_TOPIC);
                    assertThat(loggingEvent.getThrowableProxy().getMessage()).isEqualTo("broker unavailable");
                });
    }
}
