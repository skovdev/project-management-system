package local.pms.authservice.kafka.saga.producer.user;

import ch.qos.logback.classic.Logger;

import ch.qos.logback.classic.spi.ILoggingEvent;

import ch.qos.logback.core.read.ListAppender;

import local.pms.authservice.constant.KafkaConstants;

import local.pms.authservice.event.UserDetailsDeletedEvent;

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
class UserDetailsDeletionProducerTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private UserDetailsDeletionProducer producer;

    private ListAppender<ILoggingEvent> logAppender;

    @BeforeEach
    void attachLogAppender() {
        logAppender = new ListAppender<>();
        logAppender.start();
        ((Logger) LoggerFactory.getLogger(UserDetailsDeletionProducer.class)).addAppender(logAppender);
    }

    @AfterEach
    void detachLogAppender() {
        ((Logger) LoggerFactory.getLogger(UserDetailsDeletionProducer.class)).detachAppender(logAppender);
    }

    @Test
    @DisplayName("sendUserDetailsToDelete sends the event to the user-details-deletion topic")
    void should_sendEvent_when_sendUserDetailsToDeleteCalled() {
        var event = new UserDetailsDeletedEvent(UUID.randomUUID());
        var future = new SendResult<String, Object>(null, null);
        when(kafkaTemplate.send(any(String.class), any()))
                .thenReturn(CompletableFuture.completedFuture(future));

        producer.sendUserDetailsToDelete(KafkaConstants.Topics.USER_DETAILS_DELETION_TOPIC, event);

        var topicCaptor = ArgumentCaptor.forClass(String.class);
        var payloadCaptor = ArgumentCaptor.forClass(Object.class);
        verify(kafkaTemplate).send(topicCaptor.capture(), payloadCaptor.capture());

        assertThat(topicCaptor.getValue()).isEqualTo(KafkaConstants.Topics.USER_DETAILS_DELETION_TOPIC);
        assertThat(payloadCaptor.getValue()).isEqualTo(event);
    }

    @Test
    @DisplayName("sendUserDetailsToDelete sends the correct event payload")
    void should_sendCorrectEvent_when_sendUserDetailsToDeleteCalled() {
        var event = new UserDetailsDeletedEvent(UUID.randomUUID());
        var future = new SendResult<String, Object>(null, null);
        when(kafkaTemplate.send(eq(KafkaConstants.Topics.USER_DETAILS_DELETION_TOPIC), eq(event)))
                .thenReturn(CompletableFuture.completedFuture(future));

        producer.sendUserDetailsToDelete(KafkaConstants.Topics.USER_DETAILS_DELETION_TOPIC, event);

        verify(kafkaTemplate).send(KafkaConstants.Topics.USER_DETAILS_DELETION_TOPIC, event);
    }

    @Test
    @DisplayName("sendUserDetailsToDelete logs the failure with the topic instead of crashing when the Kafka send fails")
    void should_logFailureWithTopic_when_kafkaSendFails() {
        var event = new UserDetailsDeletedEvent(UUID.randomUUID());
        var failedFuture = CompletableFuture.<SendResult<String, Object>>failedFuture(new RuntimeException("broker unavailable"));
        when(kafkaTemplate.send(any(String.class), any())).thenReturn(failedFuture);

        producer.sendUserDetailsToDelete(KafkaConstants.Topics.USER_DETAILS_DELETION_TOPIC, event);

        // A null SendResult on failure previously caused an NPE inside the whenComplete callback,
        // which the JDK silently swallows: the only observable symptom is the error log never
        // being written. Asserting the log line is the only way to catch a regression of that bug.
        assertThat(logAppender.list)
                .anySatisfy(loggingEvent -> {
                    assertThat(loggingEvent.getFormattedMessage())
                            .contains("Failed to send user details deletion event")
                            .contains(KafkaConstants.Topics.USER_DETAILS_DELETION_TOPIC);
                    assertThat(loggingEvent.getThrowableProxy().getMessage()).isEqualTo("broker unavailable");
                });
    }
}
