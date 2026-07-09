package local.pms.authservice.kafka.saga.producer.user;

import local.pms.authservice.event.UserDetailsDeletedEvent;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.core.KafkaTemplate;

import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserDetailsDeletionProducer {

    final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendUserDetailsToDelete(String topic, UserDetailsDeletedEvent event) {
        log.info("Attempting to send user details deletion event to topic: {}", topic);
        this.kafkaTemplate.send(topic, event).whenComplete((result, exception) -> loggingResult(topic, exception));
    }

    private void loggingResult(String topic, Throwable exception) {
        if (exception != null) {
            log.error("Failed to send user details deletion event to topic: {}", topic, exception);
        } else {
            log.info("User details deletion event sent successfully to topic: {}", topic);
        }
    }
}
