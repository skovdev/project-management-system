package local.pms.authservice.kafka.saga.producer.user;

import com.fasterxml.jackson.core.JsonProcessingException;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.core.KafkaTemplate;

import org.springframework.kafka.support.SendResult;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.HashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserDetailsDeletionProducer {

    private static final String AUTH_USER_ID_KEY = "authUserId";

    final ObjectMapper objectMapper;
    final KafkaTemplate<String, String> kafkaTemplate;

    public void sendUserDetailsToDelete(String topic, UUID authUserId) {
        Map<String, Object> data = buildDataMap(authUserId);
        try {
            log.info("Attempting to send an authentication identifier to topic: {}", topic);
            String message = objectMapper.writeValueAsString(data);
            this.kafkaTemplate.send(topic, message).whenComplete(this::loggingResult);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize the json object. Error: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to serialize the json object: " + e.getMessage(), e);
        }
    }

    private Map<String, Object> buildDataMap(UUID authUserId) {
        Map<String, Object> data = new HashMap<>();
        data.put(AUTH_USER_ID_KEY, authUserId);
        return data;
    }

    private void loggingResult(SendResult<String, String> result, Throwable exception) {
        if (exception != null) {
            log.error("Failed to send an authentication identifier to topic: {}. Exception: {}", result.getRecordMetadata().topic(), exception.getMessage());
        } else {
            log.info("An authentication identifier sent successfully to topic: {}", result.getRecordMetadata().topic());
        }
    }
}
