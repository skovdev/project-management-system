package local.pms.userservice.kafka.saga.consumer;

import local.pms.userservice.constant.KafkaConstants;

import local.pms.userservice.dto.UserDto;

import local.pms.userservice.event.UserDetailsCreatedEvent;

import local.pms.userservice.service.UserService;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.annotation.KafkaListener;

import org.springframework.kafka.core.KafkaTemplate;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

import java.util.UUID;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserDetailsCreationConsumer {

    private final UserService userService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = KafkaConstants.Topics.USER_DETAILS_CREATION_TOPIC, groupId = KafkaConstants.GroupIds.USER_DETAILS_CREATION_GROUP_ID)
    public void receiveUserDataToCreate(UserDetailsCreatedEvent event) {
        UUID authUserId = event.userDetailsDto().authUserId();
        log.info("Received user data to create. Topic: {} - Datetime: {}", KafkaConstants.Topics.USER_DETAILS_CREATION_TOPIC, LocalDateTime.now());

        if (userService.existsByAuthUserIdIncludingDeleted(authUserId)) {
            log.warn("Idempotency check: user with authUserId {} already exists. Skipping duplicate message.", authUserId);
            return;
        }

        try {
            var userDto = buildUserDto(event);
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
        log.warn("Publishing compensation event to rollback auth user creation. AuthUserID: {}", authUserId);
        try {
            kafkaTemplate.send(KafkaConstants.Topics.USER_DETAILS_CREATION_FAILED_TOPIC, event)
                    .get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Failed to send compensation event for authUserId {}. Rethrowing for DLQ handling.", authUserId, e);
            throw new RuntimeException("Compensation event send failed for authUserId: " + authUserId, e);
        }
    }
}