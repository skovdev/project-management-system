package local.pms.authservice.kafka.saga.producer.user;

import local.pms.authservice.event.UserDetailsDeletedEvent;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.core.KafkaTemplate;

import org.springframework.kafka.support.SendResult;

import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserDetailsDeletionProducer {

    final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendUserDetailsToDelete(String topic, UserDetailsDeletedEvent event) {
        log.info("Attempting to send an authentication identifier to topic: {}", topic);
        this.kafkaTemplate.send(topic, event.authUserId()).whenComplete(this::loggingResult);
    }

    private void loggingResult(SendResult<String, Object> result, Throwable exception) {
        if (exception != null) {
            log.error("Failed to send an authentication identifier to topic: {}. Exception: {}", result.getRecordMetadata().topic(), exception.getMessage());
        } else {
            log.info("An authentication identifier sent successfully to topic: {}", result.getRecordMetadata().topic());
        }
    }
}
