package local.pms.userservice.kafka.saga.consumer;

import local.pms.userservice.constant.KafkaConstants;

import local.pms.userservice.event.UserDetailsDeletedEvent;

import local.pms.userservice.service.UserService;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.annotation.KafkaListener;

import org.springframework.kafka.core.KafkaTemplate;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserDetailsDeletionConsumer {

    private final UserService userService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = KafkaConstants.Topics.USER_DETAILS_DELETION_TOPIC,
            groupId = KafkaConstants.GroupIds.USER_DETAILS_DELETION_GROUP_ID)
    public void receiveUserDataToDelete(UserDetailsDeletedEvent event) {
        log.info("Received user data to delete. Topic: {} - Datetime: {}", KafkaConstants.Topics.USER_DETAILS_DELETION_TOPIC, LocalDateTime.now());
        try {
            log.info("Attempting to delete the user data");
            userService.deleteById(event.authUserId());
        } catch (Exception e) {
            log.error("Failed to process deleting the user. AuthUserID: {}", event.authUserId());
            handleUserDetailsDeleteFailed(event);
        }
    }

    private void handleUserDetailsDeleteFailed(UserDetailsDeletedEvent event) {
        UUID authUserId = event.authUserId();
        this.kafkaTemplate.send(KafkaConstants.Topics.USER_DETAILS_DELETION_FAILED_TOPIC, authUserId);
    }
}