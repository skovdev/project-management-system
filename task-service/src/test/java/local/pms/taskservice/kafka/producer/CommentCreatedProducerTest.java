package local.pms.taskservice.kafka.producer;

import ch.qos.logback.classic.Logger;

import ch.qos.logback.classic.spi.ILoggingEvent;

import ch.qos.logback.core.read.ListAppender;

import local.pms.taskservice.constant.KafkaConstants;

import local.pms.taskservice.event.CommentCreatedEvent;

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
class CommentCreatedProducerTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private CommentCreatedProducer producer;

    private ListAppender<ILoggingEvent> logAppender;

    @BeforeEach
    void attachLogAppender() {
        logAppender = new ListAppender<>();
        logAppender.start();
        ((Logger) LoggerFactory.getLogger(CommentCreatedProducer.class)).addAppender(logAppender);
    }

    @AfterEach
    void detachLogAppender() {
        ((Logger) LoggerFactory.getLogger(CommentCreatedProducer.class)).detachAppender(logAppender);
    }

    @Test
    @DisplayName("sendCommentCreatedEvent sends event to the comment-created topic")
    void should_sendEvent_when_sendCommentCreatedEventCalled() {
        var event = buildEvent();
        var future = new SendResult<String, Object>(null, null);
        when(kafkaTemplate.send(any(String.class), any()))
                .thenReturn(CompletableFuture.completedFuture(future));

        producer.sendCommentCreatedEvent(KafkaConstants.Topics.COMMENT_CREATED_TOPIC, event);

        var topicCaptor = ArgumentCaptor.forClass(String.class);
        var eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(kafkaTemplate).send(topicCaptor.capture(), eventCaptor.capture());

        assertThat(topicCaptor.getValue()).isEqualTo(KafkaConstants.Topics.COMMENT_CREATED_TOPIC);
        assertThat(eventCaptor.getValue()).isEqualTo(event);
    }

    @Test
    @DisplayName("sendCommentCreatedEvent sends the correct event payload")
    void should_sendCorrectEvent_when_sendCommentCreatedEventCalled() {
        var event = buildEvent();
        var future = new SendResult<String, Object>(null, null);
        when(kafkaTemplate.send(eq(KafkaConstants.Topics.COMMENT_CREATED_TOPIC), eq(event)))
                .thenReturn(CompletableFuture.completedFuture(future));

        producer.sendCommentCreatedEvent(KafkaConstants.Topics.COMMENT_CREATED_TOPIC, event);

        verify(kafkaTemplate).send(KafkaConstants.Topics.COMMENT_CREATED_TOPIC, event);
    }

    @Test
    @DisplayName("sendCommentCreatedEvent logs the failure with the topic instead of crashing when the Kafka send fails")
    void should_logFailureWithTopic_when_kafkaSendFails() {
        var event = buildEvent();
        var failedFuture = CompletableFuture.<SendResult<String, Object>>failedFuture(new RuntimeException("broker unavailable"));
        when(kafkaTemplate.send(any(String.class), any())).thenReturn(failedFuture);

        producer.sendCommentCreatedEvent(KafkaConstants.Topics.COMMENT_CREATED_TOPIC, event);

        assertThat(logAppender.list)
                .anySatisfy(loggingEvent -> {
                    assertThat(loggingEvent.getFormattedMessage())
                            .contains("Failed to publish comment-created event")
                            .contains(KafkaConstants.Topics.COMMENT_CREATED_TOPIC);
                    assertThat(loggingEvent.getThrowableProxy().getMessage()).isEqualTo("broker unavailable");
                });
    }

    private CommentCreatedEvent buildEvent() {
        return new CommentCreatedEvent(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "A comment");
    }
}
