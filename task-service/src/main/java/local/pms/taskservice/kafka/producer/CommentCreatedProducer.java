package local.pms.taskservice.kafka.producer;

import local.pms.taskservice.event.CommentCreatedEvent;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.core.KafkaTemplate;

import org.springframework.kafka.support.SendResult;

import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommentCreatedProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendCommentCreatedEvent(String topic, CommentCreatedEvent event) {
        log.info("Publishing comment-created event for commentId: {} on taskId: {} to topic: {}",
                event.commentId(), event.taskId(), topic);
        kafkaTemplate.send(topic, event).whenComplete(this::logResult);
    }

    private void logResult(SendResult<String, Object> result, Throwable exception) {
        if (exception != null) {
            log.error("Failed to publish comment-created event to topic: {}. Error: {}",
                    result.getRecordMetadata().topic(), exception.getMessage());
        } else {
            log.info("Comment-created event published successfully to topic: {}",
                    result.getRecordMetadata().topic());
        }
    }
}
