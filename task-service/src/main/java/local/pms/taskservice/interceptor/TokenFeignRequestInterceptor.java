package local.pms.taskservice.interceptor;

import feign.RequestTemplate;
import feign.RequestInterceptor;

import local.pms.taskservice.service.TokenService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

/**
 * Feign interceptor that propagates the current JWT token to outbound ai-service requests.
 */
@Component
@RequiredArgsConstructor
public class TokenFeignRequestInterceptor implements RequestInterceptor {

    private final TokenService tokenService;

    @Override
    public void apply(RequestTemplate requestTemplate) {
        String token = tokenService.getToken();
        if (token != null && !token.isEmpty()) {
            requestTemplate.header("Authorization", "Bearer " + token);
        } else {
            throw new IllegalStateException("Token cannot be null or empty");
        }
    }
}
