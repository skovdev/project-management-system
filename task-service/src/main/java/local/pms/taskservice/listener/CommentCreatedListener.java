package local.pms.taskservice.listener;

import local.pms.taskservice.constant.KafkaConstants;
import local.pms.taskservice.event.CommentCreatedEvent;
import local.pms.taskservice.kafka.producer.CommentCreatedProducer;

import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Transactional event listener that publishes a Kafka message after a comment is successfully persisted.
 * Using {@link TransactionPhase#AFTER_COMMIT} guarantees that the event is only sent when the
 * database transaction has committed, preventing phantom Kafka messages for rolled-back saves.
 */
@Component
@RequiredArgsConstructor
public class CommentCreatedListener {

    private static final Logger log = LoggerFactory.getLogger(CommentCreatedListener.class);

    private final CommentCreatedProducer commentCreatedProducer;

    /**
     * Sends a {@link CommentCreatedEvent} to the Kafka topic after the enclosing transaction commits.
     *
     * @param event the event carrying the persisted comment's data
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCommentCreatedEvent(CommentCreatedEvent event) {
        log.info("Publishing CommentCreatedEvent for commentId: {} on taskId: {}", event.commentId(), event.taskId());
        commentCreatedProducer.sendCommentCreatedEvent(KafkaConstants.Topics.COMMENT_CREATED_TOPIC, event);
    }
}
