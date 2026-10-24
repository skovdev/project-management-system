package local.pms.authservice.service.impl;

import local.pms.authservice.exception.TooManyLoginAttemptsException;

import local.pms.authservice.service.LoginAttemptService;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory brute-force protection: blocks a username for {@link #BLOCK_DURATION} after
 * {@link #MAX_FAILURES} consecutive failed sign-ins. State is per instance, which is enough
 * for a single auth-service replica.
 */
@Service
public class LoginAttemptServiceImpl implements LoginAttemptService {

    public static final int MAX_FAILURES = 5;
    static final Duration BLOCK_DURATION = Duration.ofMinutes(5);

    private record Attempts(int failures, Instant lastFailure) {}

    private final Map<String, Attempts> attempts = new ConcurrentHashMap<>();

    @Override
    public void checkNotBlocked(String username) {
        var current = attempts.get(key(username));
        if (current == null) {
            return;
        }
        if (current.lastFailure().plus(BLOCK_DURATION).isBefore(Instant.now())) {
            attempts.remove(key(username));
            return;
        }
        if (current.failures() >= MAX_FAILURES) {
            throw new TooManyLoginAttemptsException("Too many failed sign-in attempts. Try again later");
        }
    }

    @Override
    public void recordFailure(String username) {
        attempts.merge(key(username), new Attempts(1, Instant.now()),
                (old, ignored) -> new Attempts(old.failures() + 1, Instant.now()));
    }

    @Override
    public void recordSuccess(String username) {
        attempts.remove(key(username));
    }

    private String key(String username) {
        return username.toLowerCase();
    }
}
