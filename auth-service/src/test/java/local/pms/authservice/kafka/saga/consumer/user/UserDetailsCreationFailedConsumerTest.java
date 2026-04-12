package local.pms.authservice.kafka.saga.consumer.user;

import local.pms.authservice.dto.authuser.UserDetailsDto;

import local.pms.authservice.event.UserDetailsCreatedEvent;

import local.pms.authservice.exception.AuthUserDeletionException;

import local.pms.authservice.service.AuthService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.InjectMocks;

import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.doNothing;

@ExtendWith(MockitoExtension.class)
class UserDetailsCreationFailedConsumerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private UserDetailsCreationFailedConsumer consumer;

    @Test
    @DisplayName("onUserDetailsCreationFailed calls deleteById when auth user is still active")
    void should_callDeleteById_when_userDetailsCreationFailed() {
        var authUserId = UUID.randomUUID();
        var event = buildEvent(authUserId);
        when(authService.isDeletedById(authUserId)).thenReturn(false);
        doNothing().when(authService).deleteById(authUserId);

        consumer.onUserDetailsCreationFailed(event);

        verify(authService).deleteById(authUserId);
    }

    @Test
    @DisplayName("onUserDetailsCreationFailed skips deleteById when auth user is already deleted (idempotency guard)")
    void should_skip_when_authUserAlreadyDeleted() {
        var authUserId = UUID.randomUUID();
        var event = buildEvent(authUserId);
        when(authService.isDeletedById(authUserId)).thenReturn(true);

        consumer.onUserDetailsCreationFailed(event);

        verify(authService, never()).deleteById(authUserId);
    }

    @Test
    @DisplayName("onUserDetailsCreationFailed wraps service failure in AuthUserDeletionException")
    void should_throwAuthUserDeletionException_when_deleteByIdFails() {
        var authUserId = UUID.randomUUID();
        var event = buildEvent(authUserId);
        when(authService.isDeletedById(authUserId)).thenReturn(false);
        doThrow(new RuntimeException("DB error")).when(authService).deleteById(authUserId);

        assertThatThrownBy(() -> consumer.onUserDetailsCreationFailed(event))
                .isInstanceOf(AuthUserDeletionException.class)
                .hasMessageContaining("Saga compensation");
    }

    private UserDetailsCreatedEvent buildEvent(UUID authUserId) {
        var dto = new UserDetailsDto("Alice", "Smith", "alice@mail.com", authUserId);
        return new UserDetailsCreatedEvent(dto);
    }
}
