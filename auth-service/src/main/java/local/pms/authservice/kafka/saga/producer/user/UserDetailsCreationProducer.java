package local.pms.authservice.kafka.saga.producer.user;

import com.fasterxml.jackson.core.JsonProcessingException;

import com.fasterxml.jackson.databind.ObjectMapper;

import local.pms.authservice.dto.SignUpDto;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import lombok.experimental.FieldDefaults;

import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.core.KafkaTemplate;

import org.springframework.kafka.support.SendResult;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.HashMap;

@Slf4j
@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class UserDetailsCreationProducer {

    private static final String FIRST_NAME_KEY = "firstName";
    private static final String LAST_NAME_KEY = "lastName";
    private static final String EMAIL_KEY = "email";
    private static final String AUTH_USER_ID_KEY = "authUserId";

    final ObjectMapper objectMapper;
    final KafkaTemplate<String, String> kafkaTemplate;

    public void sendUserDetailsToCreate(String topic, SignUpDto signUpDTO, UUID authUserId) {
        Map<String, Object> data = buildDataMap(signUpDTO, authUserId);
        try {
            log.info("Attempting to send user details to topic: {}", topic);
            String message = objectMapper.writeValueAsString(data);
            this.kafkaTemplate.send(topic, message).whenComplete(this::loggingResult);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize the json object. Error: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to serialize the json object: " + e.getMessage(), e);
        }
    }

    private Map<String, Object> buildDataMap(SignUpDto signUpDTO, UUID authUserId) {
        Map<String, Object> data = new HashMap<>();
        data.put(FIRST_NAME_KEY, signUpDTO.firstName());
        data.put(LAST_NAME_KEY, signUpDTO.lastName());
        data.put(EMAIL_KEY, signUpDTO.email());
        data.put(AUTH_USER_ID_KEY, authUserId);
        return data;
    }

    private void loggingResult(SendResult<String, String> result, Throwable exception) {
        if (exception != null) {
            log.error("Failed to send user details to topic: {}. Exception: {}", result.getRecordMetadata().topic(), exception.getMessage());
        } else {
            log.info("User details sent successfully to topic: {}", result.getRecordMetadata().topic());
        }
    }
}
