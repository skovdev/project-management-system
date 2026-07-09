package local.pms.projectservice.listener;

import local.pms.projectservice.constant.KafkaConstants;

import local.pms.projectservice.event.ProjectCreatedEvent;

import local.pms.projectservice.kafka.producer.ProjectCreatedProducer;

import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;

import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Transactional event listener that publishes a Kafka message after a project is successfully persisted.
 * Using {@link TransactionPhase#AFTER_COMMIT} guarantees that the event is only sent when the
 * database transaction has committed, preventing phantom Kafka messages for rolled-back saves.
 */
@Component
@RequiredArgsConstructor
public class ProjectCreatedListener {

    private static final Logger log = LoggerFactory.getLogger(ProjectCreatedListener.class);

    private final ProjectCreatedProducer projectCreatedProducer;

    /**
     * Sends a {@link ProjectCreatedEvent} to the Kafka topic after the enclosing transaction commits.
     *
     * @param event the event carrying the persisted project's data
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProjectCreatedEvent(ProjectCreatedEvent event) {
        log.info("Publishing ProjectCreatedEvent for projectId: {}", event.projectId());
        projectCreatedProducer.sendProjectCreatedEvent(KafkaConstants.Topics.PROJECT_CREATED_TOPIC, event);
    }
}
