package local.pms.apigateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;

import local.pms.apigateway.config.jwt.JwtTokenProvider;

import local.pms.apigateway.dto.ErrorResponse;

import local.pms.apigateway.exception.InvalidJwtException;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;

import org.springframework.core.Ordered;

import org.springframework.core.io.buffer.DataBuffer;

import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;

import org.springframework.stereotype.Component;

import org.springframework.util.AntPathMatcher;

import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final int FILTER_ORDER = -1;

    private static final String BEARER_PREFIX = "Bearer ";

    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/v1/auth/sign-up",
            "/api/v1/auth/sign-in"
    );

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        var path = exchange.getRequest().getPath().value();

        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        var authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null) {
            log.warn("Request to '{}' rejected: Authorization header is missing", path);
            return writeErrorResponse(exchange, HttpStatus.UNAUTHORIZED, "Authorization header is missing");
        }

        if (!authHeader.startsWith(BEARER_PREFIX)) {
            log.warn("Request to '{}' rejected: Authorization header does not contain a Bearer token", path);
            return writeErrorResponse(exchange, HttpStatus.UNAUTHORIZED, "Bearer token is missing");
        }

        var token = authHeader.substring(BEARER_PREFIX.length());

        try {
            jwtTokenProvider.validateToken(token);
        } catch (InvalidJwtException e) {
            log.warn("Request to '{}' rejected: {}", path, e.getMessage());
            return writeErrorResponse(exchange, HttpStatus.UNAUTHORIZED, e.getMessage());
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return FILTER_ORDER;
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(publicPath -> PATH_MATCHER.match(publicPath, path));
    }

    private Mono<Void> writeErrorResponse(ServerWebExchange exchange, HttpStatus status, String message) {
        var response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        byte[] body = serializeErrorBody(status, message);
        DataBuffer buffer = response.bufferFactory().wrap(body);
        return response.writeWith(Mono.just(buffer));
    }

    private byte[] serializeErrorBody(HttpStatus status, String message) {
        try {
            var errorResponse = new ErrorResponse(status.value(), status.getReasonPhrase(), message);
            return objectMapper.writeValueAsBytes(errorResponse);
        } catch (Exception e) {
            return ("{\"message\":\"" + message + "\"}").getBytes(StandardCharsets.UTF_8);
        }
    }
}
