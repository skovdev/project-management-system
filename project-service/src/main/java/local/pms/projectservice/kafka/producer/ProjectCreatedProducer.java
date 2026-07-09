package local.pms.projectservice.kafka.producer;

import local.pms.projectservice.event.ProjectCreatedEvent;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.core.KafkaTemplate;

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
        kafkaTemplate.send(topic, event).whenComplete((result, exception) -> logResult(topic, exception));
    }

    private void logResult(String topic, Throwable exception) {
        if (exception != null) {
            log.error("Failed to publish project-created event to topic: {}", topic, exception);
        } else {
            log.info("Project-created event published successfully to topic: {}", topic);
        }
    }
}
