package local.pms.authservice.kafka.saga.consumer.user;

import local.pms.authservice.constant.KafkaConstants;

import local.pms.authservice.event.UserDetailsDeletedEvent;

import local.pms.authservice.service.AuthService;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.annotation.KafkaListener;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserDetailsDeletionFailedConsumer {

    public final AuthService authService;

    @KafkaListener(topics = KafkaConstants.Topics.USER_DETAILS_DELETION_FAILED_TOPIC,
            groupId = KafkaConstants.GroupIds.AUTH_USER_DETAILS_DELETION_GROUP_ID)
    public void onUserDetailsDeletionFailed(UserDetailsDeletedEvent event) {
        UUID authUserId = event.authUserId();
        try {
            log.info("User details deletion failed. Attempting to restore the auth user with ID: {}", authUserId);
            authService.restoreAuthUserById(authUserId);
        } catch (Exception e) {
            log.error("Failed to restore the auth user. AuthUserID: {}", authUserId, e);
            throw new RuntimeException("Failed to restore the auth user", e);
        }
    }
}
