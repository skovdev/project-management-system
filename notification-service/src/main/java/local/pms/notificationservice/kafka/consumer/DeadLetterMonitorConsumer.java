package local.pms.notificationservice.kafka.consumer;

import local.pms.notificationservice.constant.KafkaConstants;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import org.apache.kafka.common.header.Header;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.kafka.annotation.KafkaListener;

import org.springframework.kafka.support.KafkaHeaders;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * Logs an alert for every message that exhausts its retries and lands on a dead-letter topic,
 * so a missed notification is visible in the logs instead of failing silently forever.
 */
@Component
public class DeadLetterMonitorConsumer {

    private static final Logger log = LoggerFactory.getLogger(DeadLetterMonitorConsumer.class);
    private static final String UNKNOWN = "unknown";

    /**
     * Handles a dead-lettered record from any of this service's consumed topics.
     *
     * @param record the raw dead-letter record; consumed as plain strings since the original
     *               message may have died because it failed JSON deserialization in the first place
     */
    @KafkaListener(
            topics = {
                    KafkaConstants.Topics.USER_DETAILS_CREATION_DLT_TOPIC,
                    KafkaConstants.Topics.PROJECT_CREATED_DLT_TOPIC,
                    KafkaConstants.Topics.TASK_CREATED_DLT_TOPIC
            },
            groupId = KafkaConstants.GroupIds.NOTIFICATION_DLT_MONITOR_GROUP_ID,
            containerFactory = "dltKafkaListenerContainerFactory")
    public void onDeadLetter(ConsumerRecord<String, String> record) {
        // The payload is deliberately excluded: the user-details-creation.DLT topic carries
        // UserDetailsDto, which includes PII (name, email) that must never be written to logs.
        log.error("""
                DLT_ALERT: message exhausted retries and landed on '{}' (key='{}'). \
                Original topic: {}. Exception: {}: {}""",
                record.topic(), record.key(),
                header(record, KafkaHeaders.DLT_ORIGINAL_TOPIC),
                header(record, KafkaHeaders.DLT_EXCEPTION_FQCN),
                header(record, KafkaHeaders.DLT_EXCEPTION_MESSAGE));
    }

    private String header(ConsumerRecord<String, String> record, String headerName) {
        Header header = record.headers().lastHeader(headerName);
        return header == null ? UNKNOWN : new String(header.value(), StandardCharsets.UTF_8);
    }
}
