package local.pms.userservice.kafka.saga.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;

import com.fasterxml.jackson.core.type.TypeReference;

import com.fasterxml.jackson.databind.ObjectMapper;

import local.pms.userservice.constant.KafkaTopics;

import local.pms.userservice.service.UserService;

import local.pms.userservice.util.UUIDUtil;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import org.springframework.kafka.annotation.KafkaListener;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserDetailsDeletionConsumer {

    private static final String USER_DEFAULT_GROUP_ID = "user-default-group-id";
    private static final String AUTH_USER_ID_KEY = "authUserId";

    private final UserService userService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = KafkaTopics.USER_DETAILS_DELETED_TOPIC, groupId = USER_DEFAULT_GROUP_ID)
    public void receiveUserDataToDelete(ConsumerRecord<String, String> consumerRecord) {
        log.info("Received user data to delete. Topic: {} - Timestamp: {}", consumerRecord.topic(), consumerRecord.timestamp());
        deleteUserDetails(parseJsonToMap(consumerRecord));
    }

    private Map<String, Object> parseJsonToMap(ConsumerRecord<String, String> consumerRecord) {
        try {
            String json = consumerRecord.value();
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            log.error("Failed to read the json object. Error: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to read the json object: " + e.getMessage(), e);
        }
    }

    private void deleteUserDetails(Map<String, Object> dataMap) {
        try {
            UUID authUserId = UUIDUtil.getUUIDValueFromMap(AUTH_USER_ID_KEY, dataMap);
            log.info("Attempting to delete the user data");
            userService.deleteById(authUserId);
        } catch (Exception e) {
            log.error("Failed to process deleting the user. AuthUserID: {}", dataMap.get(AUTH_USER_ID_KEY));
            throw new RuntimeException("Failed to delete the user data", e);
        }
    }
}