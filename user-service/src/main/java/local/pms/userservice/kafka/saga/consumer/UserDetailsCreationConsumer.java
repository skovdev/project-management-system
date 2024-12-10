package local.pms.userservice.kafka.saga.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;

import com.fasterxml.jackson.core.type.TypeReference;

import com.fasterxml.jackson.databind.ObjectMapper;

import local.pms.userservice.constant.KafkaTopics;

import local.pms.userservice.dto.UserDto;

import local.pms.userservice.service.UserService;

import local.pms.userservice.util.MapUtil;
import local.pms.userservice.util.UUIDUtil;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import lombok.experimental.FieldDefaults;

import lombok.extern.slf4j.Slf4j;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import org.springframework.kafka.annotation.KafkaListener;

import org.springframework.kafka.core.KafkaTemplate;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.HashMap;

@Slf4j
@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class UserDetailsCreationConsumer {

    static final String USER_DEFAULT_GROUP_ID = "user-default-group-id";

    static final String FIRST_NAME_KEY = "firstName";
    static final String LAST_NAME_KEY = "lastName";
    static final String EMAIL_KEY = "email";
    static final String AUTH_USER_ID_KEY = "authUserId";

    final UserService userService;
    final KafkaTemplate<String, String> kafkaTemplate;
    final ObjectMapper objectMapper;

    @KafkaListener(topics = KafkaTopics.USER_DETAILS_CREATED_TOPIC, groupId = USER_DEFAULT_GROUP_ID)
    public void receiveUserDataToCreate(ConsumerRecord<String, String> consumerRecord) {
        log.info("Received user data to create. Topic: {} - Timestamp: {}", consumerRecord.topic(), consumerRecord.timestamp());
        buildUserDetails(parseJsonToMap(consumerRecord));
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

    private void buildUserDetails(Map<String, Object> dataMap) {
        try {
            UserDto userDto = buildUserDto(dataMap);
            log.info("Attempting to save the user details");
            userService.save(userDto);
        } catch (Exception e) {
            log.error("Failed to process saving the user details. Error: {}", e.getMessage(), e);
            handleUserDetailsFailed(dataMap);
        }
    }

    private UserDto buildUserDto(Map<String, Object> dataMap) {
        return new UserDto(
                null,
                MapUtil.getValue(dataMap, FIRST_NAME_KEY, String.class),
                MapUtil.getValue(dataMap, LAST_NAME_KEY, String.class),
                MapUtil.getValue(dataMap, EMAIL_KEY, String.class),
                UUIDUtil.getUUIDValueFromMap(AUTH_USER_ID_KEY, dataMap));
    }


    private void handleUserDetailsFailed(Map<String, Object> dataMap) {
        Map<String, Object> dataToSend = prepareDataToSend(dataMap);
        try {
            String message = objectMapper.writeValueAsString(dataToSend);
            this.kafkaTemplate.send(KafkaTopics.USER_DETAILS_FAILED_TOPIC, message);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize the json object. Error: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to serialize the json object: " + e.getMessage(), e);
        }
    }

    private Map<String, Object> prepareDataToSend(Map<String, Object> dataMap) {
        Map<String, Object> dataToSend = new HashMap<>();
        dataToSend.put(AUTH_USER_ID_KEY, UUIDUtil.getUUIDValueFromMap(AUTH_USER_ID_KEY, dataMap));
        return dataToSend;
    }
}