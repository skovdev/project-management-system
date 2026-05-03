package local.pms.apigateway.config.security;

import local.pms.apigateway.config.jwt.JwtTokenProvider;

import local.pms.apigateway.exception.InvalidJwtException;

import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpHeaders;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;

import org.springframework.security.web.server.context.ServerSecurityContextRepository;

import org.springframework.stereotype.Component;

import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class JwtServerSecurityContextRepository implements ServerSecurityContextRepository {

    private static final Logger log = LoggerFactory.getLogger(JwtServerSecurityContextRepository.class);
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Mono<Void> save(ServerWebExchange exchange, SecurityContext context) {
        return Mono.empty();
    }

    @Override
    public Mono<SecurityContext> load(ServerWebExchange exchange) {
        var authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            return Mono.empty();
        }

        var token = authHeader.substring(BEARER_PREFIX.length());

        try {
            jwtTokenProvider.validateToken(token);

            var authorities = jwtTokenProvider.extractRoles(token).stream()
                    .map(SimpleGrantedAuthority::new)
                    .toList();

            var authentication = new UsernamePasswordAuthenticationToken(
                    jwtTokenProvider.extractUsername(token),
                    null,
                    authorities
            );

            return Mono.just(new SecurityContextImpl(authentication));

        } catch (InvalidJwtException e) {
            log.warn("JWT validation failed for request to '{}': {}",
                    exchange.getRequest().getPath().value(), e.getMessage());
            return Mono.empty();
        }
    }
}
