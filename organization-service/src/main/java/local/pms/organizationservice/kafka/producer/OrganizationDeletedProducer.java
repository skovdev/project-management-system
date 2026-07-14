package local.pms.organizationservice.kafka.producer;

import local.pms.organizationservice.event.OrganizationDeletedEvent;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.core.KafkaTemplate;

import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrganizationDeletedProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendOrganizationDeletedEvent(String topic, OrganizationDeletedEvent event) {
        log.info("Publishing organization-deleted event for organizationId: {} to topic: {}", event.organizationId(), topic);
        kafkaTemplate.send(topic, event).whenComplete((result, exception) -> logResult(topic, exception));
    }

    private void logResult(String topic, Throwable exception) {
        if (exception != null) {
            log.error("Failed to publish organization-deleted event to topic: {}", topic, exception);
        } else {
            log.info("Organization-deleted event published successfully to topic: {}", topic);
        }
    }
}
