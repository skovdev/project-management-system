package local.pms.userservice.kafka.saga.consumer;

import local.pms.userservice.constant.KafkaConstants;

import local.pms.userservice.event.UserDetailsDeletedEvent;

import local.pms.userservice.exception.UserNotFoundException;

import local.pms.userservice.service.UserService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.kafka.core.KafkaTemplate;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.doNothing;

@ExtendWith(MockitoExtension.class)
class UserDetailsDeletionConsumerTest {

    @Mock
    private UserService userService;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private UserDetailsDeletionConsumer consumer;

    @Test
    @DisplayName("receiveUserDataToDelete calls userService.deleteByAuthUserId with correct authUserId")
    void should_callDeleteByAuthUserId_when_receiveUserDataToDelete() {
        var authUserId = UUID.randomUUID();
        var event = new UserDetailsDeletedEvent(authUserId);
        doNothing().when(userService).deleteByAuthUserId(authUserId);

        consumer.receiveUserDataToDelete(event);

        verify(userService).deleteByAuthUserId(authUserId);
    }

    @Test
    @DisplayName("receiveUserDataToDelete publishes compensation event when delete fails")
    void should_publishCompensationEvent_when_deleteFails() {
        var authUserId = UUID.randomUUID();
        var event = new UserDetailsDeletedEvent(authUserId);
        doThrow(new UserNotFoundException("User with authUserId '" + authUserId + "' not found"))
                .when(userService).deleteByAuthUserId(authUserId);

        consumer.receiveUserDataToDelete(event);

        verify(kafkaTemplate).send(
                eq(KafkaConstants.Topics.USER_DETAILS_DELETION_FAILED_TOPIC),
                eq(event));
    }

    @Test
    @DisplayName("receiveUserDataToDelete does not publish compensation when delete succeeds")
    void should_notPublishCompensationEvent_when_deleteSucceeds() {
        var authUserId = UUID.randomUUID();
        var event = new UserDetailsDeletedEvent(authUserId);
        doNothing().when(userService).deleteByAuthUserId(authUserId);

        consumer.receiveUserDataToDelete(event);

        verify(kafkaTemplate, never()).send(any(), any());
    }
}
