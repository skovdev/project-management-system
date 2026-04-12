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

import java.util.concurrent.TimeUnit;

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
        UUID authUserId = event.authUserId();

        if (!userService.existsByAuthUserId(authUserId)) {
            log.warn("Idempotency check: user with authUserId {} is already deleted or does not exist. Skipping duplicate message.", authUserId);
            return;
        }

        try {
            log.info("Attempting to delete the user data");
            userService.deleteByAuthUserId(authUserId);
            log.info("User data deleted successfully. AuthUserID: {}", authUserId);
        } catch (Exception e) {
            log.error("Failed to process deleting the user. AuthUserID: {}", authUserId, e);
            handleUserDetailsDeleteFailed(event);
        }
    }

    private void handleUserDetailsDeleteFailed(UserDetailsDeletedEvent event) {
        log.warn("Publishing compensation event to rollback auth user deletion. AuthUserID: {}", event.authUserId());
        try {
            kafkaTemplate.send(KafkaConstants.Topics.USER_DETAILS_DELETION_FAILED_TOPIC, event)
                    .get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Failed to send compensation event for authUserId {}. Rethrowing for DLQ handling.", event.authUserId(), e);
            throw new RuntimeException("Compensation event send failed for authUserId: " + event.authUserId(), e);
        }
    }
}