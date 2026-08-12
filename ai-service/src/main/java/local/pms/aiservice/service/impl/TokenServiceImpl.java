package local.pms.aiservice.service.impl;

import local.pms.aiservice.service.TokenService;

import org.springframework.stereotype.Component;

/**
 * Holds the current request's JWT bearer token in a {@link ThreadLocal} rather than a plain
 * field: this bean is a singleton, and a plain field would be shared mutable state across every
 * request thread, letting one in-flight request's token leak into or overwrite another's.
 */
@Component
public class TokenServiceImpl implements TokenService {

    private final ThreadLocal<String> token = new ThreadLocal<>();

    @Override
    public void setToken(String token) {
        this.token.set(token);
    }

    @Override
    public String getToken() {
        return token.get();
    }

    @Override
    public void clear() {
        token.remove();
    }
}
