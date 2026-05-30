package local.pms.taskservice.kafka.producer;

import local.pms.taskservice.event.TaskCreatedEvent;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.core.KafkaTemplate;

import org.springframework.kafka.support.SendResult;

import org.springframework.stereotype.Component;

/**
 * Publishes {@link TaskCreatedEvent} messages to the task-created Kafka topic.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TaskCreatedProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Sends a task-created event to the specified Kafka topic.
     *
     * @param topic the target topic name
     * @param event the event payload
     */
    public void sendTaskCreatedEvent(String topic, TaskCreatedEvent event) {
        log.info("Publishing task-created event for taskId: {} to topic: {}", event.taskId(), topic);
        kafkaTemplate.send(topic, event).whenComplete(this::logResult);
    }

    private void logResult(SendResult<String, Object> result, Throwable exception) {
        if (exception != null) {
            log.error("Failed to publish task-created event to topic: {}. Error: {}",
                    result.getRecordMetadata().topic(), exception.getMessage());
        } else {
            log.info("Task-created event published successfully to topic: {}",
                    result.getRecordMetadata().topic());
        }
    }
}
