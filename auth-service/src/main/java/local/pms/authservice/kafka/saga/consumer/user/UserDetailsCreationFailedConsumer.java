package local.pms.authservice.kafka.saga.consumer.user;

import local.pms.authservice.constant.KafkaConstants;

import local.pms.authservice.event.UserDetailsCreatedEvent;

import local.pms.authservice.service.AuthService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import lombok.experimental.FieldDefaults;

import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.annotation.KafkaListener;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class UserDetailsCreationFailedConsumer {

    final AuthService authService;

    @KafkaListener(topics = KafkaConstants.Topics.USER_DETAILS_CREATION_FAILED_TOPIC,
            groupId = KafkaConstants.GroupIds.AUTH_USER_DETAILS_CREATION_GROUP_ID)
    public void onUserDetailsCreationFailed(UserDetailsCreatedEvent event) {
        UUID authUserId = event.userDetailsDto().authUserId();
        try {
            log.info("User details is failed. Attempting to delete the auth user");
            authService.deleteById(authUserId);
        } catch (Exception e) {
            log.error("Failed to process deleting the user. AuthUserID: {}", authUserId, e);
            throw new RuntimeException("Failed to delete the auth user", e);
        }
    }
}
