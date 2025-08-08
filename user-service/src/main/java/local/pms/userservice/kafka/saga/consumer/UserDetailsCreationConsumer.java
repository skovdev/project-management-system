package local.pms.userservice.kafka.saga.consumer;

import local.pms.userservice.constant.KafkaConstants;

import local.pms.userservice.dto.UserDto;

import local.pms.userservice.event.UserDetailsCreatedEvent;

import local.pms.userservice.service.UserService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import lombok.experimental.FieldDefaults;

import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.annotation.KafkaListener;

import org.springframework.kafka.core.KafkaTemplate;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

import java.util.UUID;

@Slf4j
@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class UserDetailsCreationConsumer {

    final UserService userService;
    final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = KafkaConstants.Topics.USER_DETAILS_CREATION_TOPIC, groupId = KafkaConstants.GroupIds.USER_DETAILS_CREATION_GROUP_ID)
    public void receiveUserDataToCreate(UserDetailsCreatedEvent event) {
        log.info("Received user data to create. Topic: {} - Datetime: {}", KafkaConstants.Topics.USER_DETAILS_CREATION_TOPIC, LocalDateTime.now());
        try {
            UserDto userDto = buildUserDto(event);
            log.info("Attempting to save the user details");
            userService.save(userDto);
        } catch (Exception e) {
            log.error("Failed to process saving the user details. Error: {}", e.getMessage(), e);
            handleUserDetailsFailed(event);
        }
    }

    private UserDto buildUserDto(UserDetailsCreatedEvent event) {
        return new UserDto(null,
                event.userDetailsDto().firstName(),
                event.userDetailsDto().lastName(),
                event.userDetailsDto().email(),
                event.userDetailsDto().authUserId());
    }


    private void handleUserDetailsFailed(UserDetailsCreatedEvent event) {
        UUID authUserId = event.userDetailsDto().authUserId();
        this.kafkaTemplate.send(KafkaConstants.Topics.USER_DETAILS_CREATION_FAILED_TOPIC, authUserId);
    }
}