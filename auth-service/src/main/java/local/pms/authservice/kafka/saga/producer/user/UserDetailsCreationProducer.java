package local.pms.authservice.kafka.saga.producer.user;

import local.pms.authservice.event.UserDetailsCreatedEvent;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import lombok.experimental.FieldDefaults;

import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.core.KafkaTemplate;

import org.springframework.kafka.support.SendResult;

import org.springframework.stereotype.Component;

@Slf4j
@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class UserDetailsCreationProducer {

    final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendUserDetailsToCreate(String topic, UserDetailsCreatedEvent event) {
        log.info("Attempting to send user details to topic: {}", topic);
        this.kafkaTemplate.send(topic, event).whenComplete(this::loggingResult);
    }

    private void loggingResult(SendResult<String, Object> result, Throwable exception) {
        if (exception != null) {
            log.error("Failed to send user details to topic: {}. Exception: {}", result.getRecordMetadata().topic(), exception.getMessage());
        } else {
            log.info("User details sent successfully to topic: {}", result.getRecordMetadata().topic());
        }
    }
}
