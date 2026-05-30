package local.pms.notificationservice.kafka.consumer;

import local.pms.notificationservice.entity.Notification;

import local.pms.notificationservice.event.UserDetailsCreatedEvent;
import local.pms.notificationservice.event.UserDetailsDto;

import local.pms.notificationservice.service.NotificationService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserCreatedConsumerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private UserCreatedConsumer consumer;

    @Test
    @DisplayName("onUserCreated saves a WELCOME notification with the correct userId")
    void should_saveWelcomeNotification_when_userCreatedEventReceived() {
        var authUserId = UUID.randomUUID();
        var event = new UserDetailsCreatedEvent(
                new UserDetailsDto("John", "Doe", "john@example.com", authUserId));

        consumer.onUserCreated(event);

        var captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationService).save(captor.capture());

        var saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo(authUserId);
        assertThat(saved.getTitle()).isEqualTo("Welcome to PMS!");
        assertThat(saved.isRead()).isFalse();
    }

    @Test
    @DisplayName("onUserCreated rethrows exception so DefaultErrorHandler can route to DLT")
    void should_rethrowException_when_saveNotificationFails() {
        var authUserId = UUID.randomUUID();
        var event = new UserDetailsCreatedEvent(
                new UserDetailsDto("Jane", "Smith", "jane@example.com", authUserId));

        doThrow(new RuntimeException("DB error")).when(notificationService).save(org.mockito.ArgumentMatchers.any());

        assertThatThrownBy(() -> consumer.onUserCreated(event))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("DB error");
    }
}
