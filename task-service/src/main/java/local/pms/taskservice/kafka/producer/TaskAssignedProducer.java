package local.pms.taskservice.kafka.producer;

import local.pms.taskservice.event.TaskAssignedEvent;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.core.KafkaTemplate;

import org.springframework.stereotype.Component;

/**
 * Publishes {@link TaskAssignedEvent} messages to the task-assigned Kafka topic.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TaskAssignedProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Sends a task-assigned event to the specified Kafka topic.
     *
     * @param topic the target topic name
     * @param event the event payload
     */
    public void sendTaskAssignedEvent(String topic, TaskAssignedEvent event) {
        log.info("Publishing task-assigned event for taskId: {} to topic: {}", event.taskId(), topic);
        kafkaTemplate.send(topic, event).whenComplete((result, exception) -> logResult(topic, exception));
    }

    private void logResult(String topic, Throwable exception) {
        if (exception != null) {
            log.error("Failed to publish task-assigned event to topic: {}", topic, exception);
        } else {
            log.info("Task-assigned event published successfully to topic: {}", topic);
        }
    }
}
