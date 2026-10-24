package local.pms.authservice.service;

import local.pms.authservice.exception.TooManyLoginAttemptsException;

import local.pms.authservice.service.impl.LoginAttemptServiceImpl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LoginAttemptServiceImplTest {

    private final LoginAttemptServiceImpl service = new LoginAttemptServiceImpl();

    @Test
    @DisplayName("blocks a username after the maximum number of consecutive failures")
    void should_block_when_maxFailuresReached() {
        for (int i = 0; i < LoginAttemptServiceImpl.MAX_FAILURES; i++) {
            service.recordFailure("Alice");
        }

        assertThrows(TooManyLoginAttemptsException.class, () -> service.checkNotBlocked("alice"));
    }

    @Test
    @DisplayName("a successful sign-in resets the failure counter")
    void should_notBlock_when_successResetsFailures() {
        for (int i = 0; i < LoginAttemptServiceImpl.MAX_FAILURES; i++) {
            service.recordFailure("alice");
        }
        service.recordSuccess("alice");

        assertDoesNotThrow(() -> service.checkNotBlocked("alice"));
    }
}
