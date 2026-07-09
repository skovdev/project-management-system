package local.pms.taskservice.listener;

import local.pms.taskservice.constant.KafkaConstants;

import local.pms.taskservice.event.TaskCreatedEvent;

import local.pms.taskservice.kafka.producer.TaskCreatedProducer;

import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;

import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Transactional event listener that publishes a Kafka message after a task is successfully persisted.
 * Using {@link TransactionPhase#AFTER_COMMIT} guarantees that the event is only sent when the
 * database transaction has committed, preventing phantom Kafka messages for rolled-back saves.
 */
@Component
@RequiredArgsConstructor
public class TaskCreatedListener {

    private static final Logger log = LoggerFactory.getLogger(TaskCreatedListener.class);

    private final TaskCreatedProducer taskCreatedProducer;

    /**
     * Sends a {@link TaskCreatedEvent} to the Kafka topic after the enclosing transaction commits.
     *
     * @param event the event carrying the persisted task's data
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTaskCreatedEvent(TaskCreatedEvent event) {
        log.info("Publishing TaskCreatedEvent for taskId: {}", event.taskId());
        taskCreatedProducer.sendTaskCreatedEvent(KafkaConstants.Topics.TASK_CREATED_TOPIC, event);
    }
}
