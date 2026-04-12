package local.pms.authservice.kafka.saga.consumer.user;

import local.pms.authservice.constant.KafkaConstants;

import local.pms.authservice.event.UserDetailsCreatedEvent;

import local.pms.authservice.exception.AuthUserDeletionException;

import local.pms.authservice.service.AuthService;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.annotation.KafkaListener;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserDetailsCreationFailedConsumer {

    private final AuthService authService;

    @KafkaListener(topics = KafkaConstants.Topics.USER_DETAILS_CREATION_FAILED_TOPIC,
            groupId = KafkaConstants.GroupIds.AUTH_USER_DETAILS_CREATION_GROUP_ID)
    public void onUserDetailsCreationFailed(UserDetailsCreatedEvent event) {
        UUID authUserId = event.userDetailsDto().authUserId();

        if (authService.isDeletedById(authUserId)) {
            log.warn("Idempotency check: auth user with id {} is already deleted. Skipping duplicate compensation.", authUserId);
            return;
        }

        try {
            log.info("User details creation failed for authUserId: {}. Attempting to rollback by deleting the auth user.", authUserId);
            authService.deleteById(authUserId);
        } catch (Exception e) {
            log.error("Failed to delete the auth user during Saga compensation. AuthUserID: {}", authUserId, e);
            throw new AuthUserDeletionException("Failed to delete the auth user during Saga compensation", e);
        }
    }
}
