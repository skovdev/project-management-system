package local.pms.projectservice.listener;

import local.pms.projectservice.constant.KafkaConstants;

import local.pms.projectservice.event.ProjectDeletedEvent;

import local.pms.projectservice.kafka.producer.ProjectDeletedProducer;

import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;

import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Transactional event listener that publishes a Kafka message after a project is successfully deleted.
 * Using {@link TransactionPhase#AFTER_COMMIT} guarantees that the event is only sent when the
 * database transaction has committed, preventing task-service from cascading a deletion for a
 * project that a rolled-back transaction never actually removed.
 */
@Component
@RequiredArgsConstructor
public class ProjectDeletedListener {

    private static final Logger log = LoggerFactory.getLogger(ProjectDeletedListener.class);

    private final ProjectDeletedProducer projectDeletedProducer;

    /**
     * Sends a {@link ProjectDeletedEvent} to the Kafka topic after the enclosing transaction commits.
     *
     * @param event the event carrying the deleted project's ID
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProjectDeletedEvent(ProjectDeletedEvent event) {
        log.info("Publishing ProjectDeletedEvent for projectId: {}", event.projectId());
        projectDeletedProducer.sendProjectDeletedEvent(KafkaConstants.Topics.PROJECT_DELETED_TOPIC, event);
    }
}
