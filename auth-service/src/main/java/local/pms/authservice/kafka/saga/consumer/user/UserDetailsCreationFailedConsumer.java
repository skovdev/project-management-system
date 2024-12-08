package local.pms.authservice.kafka.saga.consumer.user;

import com.fasterxml.jackson.core.JsonProcessingException;

import com.fasterxml.jackson.core.type.TypeReference;

import com.fasterxml.jackson.databind.ObjectMapper;

import local.pms.authservice.constant.KafkaTopics;

import local.pms.authservice.service.AuthService;

import local.pms.authservice.util.UUIDUtil;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import lombok.experimental.FieldDefaults;

import lombok.extern.slf4j.Slf4j;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import org.springframework.kafka.annotation.KafkaListener;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class UserDetailsCreationFailedConsumer {

    static final String AUTH_SERVER_GROUP_ID = "auth-user-default-group-id";
    static final String AUTH_USER_ID_KEY = "authUserId";

    final AuthService authService;
    final ObjectMapper objectMapper;

    @KafkaListener(topics = KafkaTopics.USER_DETAILS_FAILED_TOPIC, groupId = AUTH_SERVER_GROUP_ID)
    public void onUserDetailsFailed(ConsumerRecord<String, String> consumerRecord) {
        log.info("User details is failed. Attempting to delete the auth user");
        deleteAuthUser(parseJsonToMap(consumerRecord));;
    }

    private void deleteAuthUser(Map<String, Object> dataMap) {
        try {
            log.info("Attempting to delete the auth user");
            UUID authUserId = getAuthUserId(dataMap);
            authService.deleteById(authUserId);
        } catch (Exception e) {
            log.error("Failed to process deleting the user. AuthUserID: {}", dataMap.get(AUTH_USER_ID_KEY));
            throw new RuntimeException("Failed to delete the auth user", e);
        }
    }

    private Map<String, Object> parseJsonToMap(ConsumerRecord<String, String> consumerRecord) {
        try {
            String json = consumerRecord.value();
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            log.error("Failed to parse the json object. Error: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to parse the json object: " + e.getMessage(), e);
        }
    }

    private UUID getAuthUserId(Map<String, Object> dataMap) {
        return UUIDUtil.getUUIDValueFromMap(AUTH_USER_ID_KEY, dataMap);
    }
}
