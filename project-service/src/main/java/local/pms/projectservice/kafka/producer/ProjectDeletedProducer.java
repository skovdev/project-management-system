package local.pms.projectservice.kafka.producer;

import local.pms.projectservice.event.ProjectDeletedEvent;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.core.KafkaTemplate;

import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProjectDeletedProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendProjectDeletedEvent(String topic, ProjectDeletedEvent event) {
        log.info("Publishing project-deleted event for projectId: {} to topic: {}", event.projectId(), topic);
        kafkaTemplate.send(topic, event).whenComplete((result, exception) -> logResult(topic, exception));
    }

    private void logResult(String topic, Throwable exception) {
        if (exception != null) {
            log.error("Failed to publish project-deleted event to topic: {}", topic, exception);
        } else {
            log.info("Project-deleted event published successfully to topic: {}", topic);
        }
    }
}
