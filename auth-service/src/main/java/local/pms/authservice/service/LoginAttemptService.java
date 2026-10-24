package local.pms.authservice.service;

public interface LoginAttemptService {
    void checkNotBlocked(String username);
    void recordFailure(String username);
    void recordSuccess(String username);
}
