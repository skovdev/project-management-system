package local.pms.notificationservice.service.impl;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class TokenServiceImplTest {

    @Test
    void should_isolateTokenPerThread_and_clearOnDemand() throws InterruptedException {
        var tokenService = new TokenServiceImpl();
        tokenService.setToken("main-thread-token");

        var otherThreadToken = new AtomicReference<String>();
        var otherThreadTokenAfterClear = new AtomicReference<String>();
        var thread = new Thread(() -> {
            tokenService.setToken("other-thread-token");
            otherThreadToken.set(tokenService.getToken());
            tokenService.clear();
            otherThreadTokenAfterClear.set(tokenService.getToken());
        });
        thread.start();
        thread.join();

        assertThat(otherThreadToken.get()).isEqualTo("other-thread-token");
        assertThat(otherThreadTokenAfterClear.get()).isNull();
        assertThat(tokenService.getToken()).isEqualTo("main-thread-token");
    }
}
