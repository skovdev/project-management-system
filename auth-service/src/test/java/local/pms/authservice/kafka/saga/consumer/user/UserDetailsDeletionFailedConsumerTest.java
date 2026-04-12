package local.pms.authservice.kafka.saga.consumer.user;

import local.pms.authservice.event.UserDetailsDeletedEvent;

import local.pms.authservice.exception.AuthUserNotFoundException;
import local.pms.authservice.exception.AuthUserRestoreException;

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
class UserDetailsDeletionFailedConsumerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private UserDetailsDeletionFailedConsumer consumer;

    @Test
    @DisplayName("onUserDetailsDeletionFailed calls restoreAuthUserById when auth user is deleted")
    void should_callRestoreAuthUserById_when_userDetailsDeletionFailed() {
        var authUserId = UUID.randomUUID();
        var event = new UserDetailsDeletedEvent(authUserId);
        when(authService.isDeletedById(authUserId)).thenReturn(true);
        doNothing().when(authService).restoreAuthUserById(authUserId);

        consumer.onUserDetailsDeletionFailed(event);

        verify(authService).restoreAuthUserById(authUserId);
    }

    @Test
    @DisplayName("onUserDetailsDeletionFailed skips restore when auth user is already active (idempotency guard)")
    void should_skip_when_authUserAlreadyActive() {
        var authUserId = UUID.randomUUID();
        var event = new UserDetailsDeletedEvent(authUserId);
        when(authService.isDeletedById(authUserId)).thenReturn(false);

        consumer.onUserDetailsDeletionFailed(event);

        verify(authService, never()).restoreAuthUserById(authUserId);
    }

    @Test
    @DisplayName("onUserDetailsDeletionFailed wraps service failure in AuthUserRestoreException")
    void should_throwAuthUserRestoreException_when_restoreFails() {
        var authUserId = UUID.randomUUID();
        var event = new UserDetailsDeletedEvent(authUserId);
        when(authService.isDeletedById(authUserId)).thenReturn(true);
        doThrow(new AuthUserNotFoundException("not found"))
                .when(authService).restoreAuthUserById(authUserId);

        assertThatThrownBy(() -> consumer.onUserDetailsDeletionFailed(event))
                .isInstanceOf(AuthUserRestoreException.class)
                .hasMessageContaining("Saga compensation");
    }
}
