package local.pms.organizationservice.listener;

import local.pms.organizationservice.constant.KafkaConstants;

import local.pms.organizationservice.event.OrganizationDeletedEvent;

import local.pms.organizationservice.kafka.producer.OrganizationDeletedProducer;

import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;

import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Transactional event listener that publishes a Kafka message after an organization is successfully
 * deleted. Using {@link TransactionPhase#AFTER_COMMIT} guarantees the event is only sent once the
 * database transaction has committed, preventing project-service from cascading a deletion for an
 * organization that a rolled-back transaction never actually removed.
 */
@Component
@RequiredArgsConstructor
public class OrganizationDeletedListener {

    private static final Logger log = LoggerFactory.getLogger(OrganizationDeletedListener.class);

    private final OrganizationDeletedProducer organizationDeletedProducer;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrganizationDeletedEvent(OrganizationDeletedEvent event) {
        log.info("Publishing OrganizationDeletedEvent for organizationId: {}", event.organizationId());
        organizationDeletedProducer.sendOrganizationDeletedEvent(KafkaConstants.Topics.ORGANIZATION_DELETED_TOPIC, event);
    }
}
