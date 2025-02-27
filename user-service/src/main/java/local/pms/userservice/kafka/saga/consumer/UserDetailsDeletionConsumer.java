package local.pms.userservice.kafka.saga.consumer;

import local.pms.userservice.constant.KafkaTopics;

import local.pms.userservice.event.UserDetailsDeletedEvent;

import local.pms.userservice.service.UserService;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.annotation.KafkaListener;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserDetailsDeletionConsumer {

    private static final String USER_DEFAULT_GROUP_ID = "user-default-group-id";

    private final UserService userService;

    @KafkaListener(topics = KafkaTopics.USER_DETAILS_DELETED_TOPIC, groupId = USER_DEFAULT_GROUP_ID)
    public void receiveUserDataToDelete(UserDetailsDeletedEvent event) {
        log.info("Received user data to delete. Topic: {} - Datetime: {}", KafkaTopics.USER_DETAILS_DELETED_TOPIC, LocalDateTime.now());
        try {
            log.info("Attempting to delete the user data");
            userService.deleteById(event.authUserId());
        } catch (Exception e) {
            log.error("Failed to process deleting the user. AuthUserID: {}", event.authUserId());
            throw new RuntimeException("Failed to delete the user data", e);
        }
    }
}