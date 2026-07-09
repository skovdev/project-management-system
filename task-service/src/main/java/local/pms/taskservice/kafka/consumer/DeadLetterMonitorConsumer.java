package local.pms.taskservice.kafka.consumer;

import local.pms.taskservice.constant.KafkaConstants;

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
 * so a stuck cascading-delete never fails silently forever.
 */
@Component
public class DeadLetterMonitorConsumer {

    private static final Logger log = LoggerFactory.getLogger(DeadLetterMonitorConsumer.class);
    private static final String UNKNOWN = "unknown";

    /**
     * Handles a dead-lettered record from this service's consumed topics.
     *
     * @param record the raw dead-letter record; consumed as plain strings since the original
     *               message may have died because it failed JSON deserialization in the first place
     */
    @KafkaListener(
            topics = KafkaConstants.Topics.PROJECT_DELETED_DLT_TOPIC,
            groupId = KafkaConstants.GroupIds.TASK_DLT_MONITOR_GROUP_ID,
            containerFactory = "dltKafkaListenerContainerFactory")
    public void onDeadLetter(ConsumerRecord<String, String> record) {
        log.error("""
                DLT_ALERT: message exhausted retries and landed on '{}' (key='{}'). \
                Original topic: {}. Exception: {}: {}. Payload: {}""",
                record.topic(), record.key(),
                header(record, KafkaHeaders.DLT_ORIGINAL_TOPIC),
                header(record, KafkaHeaders.DLT_EXCEPTION_FQCN),
                header(record, KafkaHeaders.DLT_EXCEPTION_MESSAGE),
                record.value());
    }

    private String header(ConsumerRecord<String, String> record, String headerName) {
        Header header = record.headers().lastHeader(headerName);
        return header == null ? UNKNOWN : new String(header.value(), StandardCharsets.UTF_8);
    }
}
