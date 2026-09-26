package local.pms.taskservice.listener;

import local.pms.taskservice.constant.KafkaConstants;

import local.pms.taskservice.event.TaskAssignedEvent;

import local.pms.taskservice.kafka.producer.TaskAssignedProducer;

import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;

import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Transactional event listener that publishes a Kafka message after a task assignment is
 * successfully committed. Using {@link TransactionPhase#AFTER_COMMIT} guarantees that the event
 * is only sent when the database transaction has committed, preventing phantom Kafka messages
 * for rolled-back saves.
 */
@Component
@RequiredArgsConstructor
public class TaskAssignedListener {

    private static final Logger log = LoggerFactory.getLogger(TaskAssignedListener.class);

    private final TaskAssignedProducer taskAssignedProducer;

    /**
     * Sends a {@link TaskAssignedEvent} to the Kafka topic after the enclosing transaction commits.
     *
     * @param event the event carrying the assigned task's data
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTaskAssignedEvent(TaskAssignedEvent event) {
        log.info("Publishing TaskAssignedEvent for taskId: {}", event.taskId());
        taskAssignedProducer.sendTaskAssignedEvent(KafkaConstants.Topics.TASK_ASSIGNED_TOPIC, event);
    }
}
