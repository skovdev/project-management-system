package local.pms.apigateway.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;

import local.pms.apigateway.dto.ErrorResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpMethod;

import org.springframework.security.web.server.header.XFrameOptionsServerHttpHeadersWriter;

import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;

import org.springframework.security.config.web.server.ServerHttpSecurity;

import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;

import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private static final long HSTS_MAX_AGE_DAYS = 365L;

    private static final String[] PUBLIC_PATHS = {
            "/api/v1/auth/sign-up",
            "/api/v1/auth/sign-in",
            "/actuator/health"
    };

    private final JwtServerSecurityContextRepository jwtServerSecurityContextRepository;
    private final ObjectMapper objectMapper;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .cors(ServerHttpSecurity.CorsSpec::disable)
                .securityContextRepository(jwtServerSecurityContextRepository)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jsonUnauthorizedEntryPoint())
                )
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(HttpMethod.OPTIONS).permitAll()
                        .pathMatchers(PUBLIC_PATHS).permitAll()
                        .anyExchange().authenticated()
                )
                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'"))
                        .frameOptions(frame -> frame.mode(XFrameOptionsServerHttpHeadersWriter.Mode.DENY))
                        .hsts(hsts -> hsts.includeSubdomains(true).maxAge(java.time.Duration.ofDays(HSTS_MAX_AGE_DAYS)))
                )
                .build();
    }

    private ServerAuthenticationEntryPoint jsonUnauthorizedEntryPoint() {
        return (exchange, ex) -> {
            var response = exchange.getResponse();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

            var errorResponse = new ErrorResponse(
                    HttpStatus.UNAUTHORIZED.value(),
                    HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                    "Authentication required: " + ex.getMessage()
            );

            return Mono.fromCallable(() -> objectMapper.writeValueAsBytes(errorResponse))
                    .flatMap(body -> {
                        var buffer = response.bufferFactory().wrap(body);
                        return response.writeWith(Mono.just(buffer));
                    });
        };
    }
}
