package local.pms.projectservice.kafka.producer;

import local.pms.projectservice.event.ProjectCreatedEvent;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.core.KafkaTemplate;

import org.springframework.kafka.support.SendResult;

import org.springframework.stereotype.Component;

/**
 * Publishes {@link ProjectCreatedEvent} messages to the project-created Kafka topic.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProjectCreatedProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Sends a project-created event to the specified Kafka topic.
     *
     * @param topic the target topic name
     * @param event the event payload
     */
    public void sendProjectCreatedEvent(String topic, ProjectCreatedEvent event) {
        log.info("Publishing project-created event for projectId: {} to topic: {}", event.projectId(), topic);
        kafkaTemplate.send(topic, event).whenComplete(this::logResult);
    }

    private void logResult(SendResult<String, Object> result, Throwable exception) {
        if (exception != null) {
            log.error("Failed to publish project-created event to topic: {}. Error: {}",
                    result.getRecordMetadata().topic(), exception.getMessage());
        } else {
            log.info("Project-created event published successfully to topic: {}",
                    result.getRecordMetadata().topic());
        }
    }
}
