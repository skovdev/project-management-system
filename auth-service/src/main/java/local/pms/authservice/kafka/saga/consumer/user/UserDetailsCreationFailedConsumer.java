package local.pms.authservice.kafka.saga.consumer.user;

import local.pms.authservice.constant.KafkaTopics;

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

    static final String AUTH_SERVER_GROUP_ID = "auth-user-default-group-id";

    final AuthService authService;

    @KafkaListener(topics = KafkaTopics.USER_DETAILS_FAILED_TOPIC, groupId = AUTH_SERVER_GROUP_ID)
    public void onUserDetailsFailed(UserDetailsCreatedEvent event) {
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
