package local.pms.projectservice.interceptor;

import feign.RequestTemplate;
import feign.RequestInterceptor;

import local.pms.projectservice.service.TokenService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

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
