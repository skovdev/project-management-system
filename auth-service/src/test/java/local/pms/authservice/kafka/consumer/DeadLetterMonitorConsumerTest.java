package local.pms.authservice.kafka.consumer;

import ch.qos.logback.classic.Logger;

import ch.qos.logback.classic.spi.ILoggingEvent;

import ch.qos.logback.core.read.ListAppender;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import org.apache.kafka.common.header.internals.RecordHeader;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import org.slf4j.LoggerFactory;

import org.springframework.kafka.support.KafkaHeaders;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class DeadLetterMonitorConsumerTest {

    private final DeadLetterMonitorConsumer consumer = new DeadLetterMonitorConsumer();

    private ListAppender<ILoggingEvent> logAppender;

    @BeforeEach
    void attachLogAppender() {
        logAppender = new ListAppender<>();
        logAppender.start();
        ((Logger) LoggerFactory.getLogger(DeadLetterMonitorConsumer.class)).addAppender(logAppender);
    }

    @AfterEach
    void detachLogAppender() {
        ((Logger) LoggerFactory.getLogger(DeadLetterMonitorConsumer.class)).detachAppender(logAppender);
    }

    @Test
    @DisplayName("onDeadLetter logs the original topic, exception details and payload")
    void should_logDeadLetterDetails_when_recordReceived() {
        var record = new ConsumerRecord<>("user-details-creation-failed.DLT", 0, 0L, "auth-user-id-123", "{\"authUserId\":\"auth-user-id-123\"}");
        record.headers()
                .add(new RecordHeader(KafkaHeaders.DLT_ORIGINAL_TOPIC, "user-details-creation-failed".getBytes(StandardCharsets.UTF_8)))
                .add(new RecordHeader(KafkaHeaders.DLT_EXCEPTION_FQCN, "java.lang.RuntimeException".getBytes(StandardCharsets.UTF_8)))
                .add(new RecordHeader(KafkaHeaders.DLT_EXCEPTION_MESSAGE, "auth user not found".getBytes(StandardCharsets.UTF_8)));

        consumer.onDeadLetter(record);

        assertThat(logAppender.list)
                .anySatisfy(loggingEvent -> assertThat(loggingEvent.getFormattedMessage())
                        .contains("user-details-creation-failed.DLT")
                        .contains("auth-user-id-123")
                        .contains("user-details-creation-failed")
                        .contains("java.lang.RuntimeException")
                        .contains("auth user not found"));
    }

    @Test
    @DisplayName("onDeadLetter does not throw when DLT headers are missing")
    void should_notThrow_when_headersMissing() {
        var record = new ConsumerRecord<>("user-details-deletion-failed.DLT", 0, 0L, "auth-user-id-456", "raw-payload");

        consumer.onDeadLetter(record);

        assertThat(logAppender.list)
                .anySatisfy(loggingEvent -> assertThat(loggingEvent.getFormattedMessage()).contains("unknown"));
    }
}
