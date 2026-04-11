package local.pms.apigateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;

import local.pms.apigateway.config.jwt.JwtTokenProvider;
import local.pms.apigateway.exception.InvalidJwtException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;

import org.springframework.http.HttpStatus;

import org.springframework.mock.http.server.reactive.MockServerHttpRequest;

import org.springframework.mock.web.server.MockServerWebExchange;

import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.doNothing;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private GatewayFilterChain chain;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(jwtTokenProvider, new ObjectMapper());
    }

    @Test
    @DisplayName("POST /api/v1/auth/sign-up bypasses JWT validation and proceeds")
    void should_proceedWithoutValidation_when_signUpPath() {
        when(chain.filter(any())).thenReturn(Mono.empty());
        var exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/api/v1/auth/sign-up").build());

        filter.filter(exchange, chain).block();

        verify(jwtTokenProvider, never()).validateToken(any());
        verify(chain).filter(any());
    }

    @Test
    @DisplayName("POST /api/v1/auth/sign-in bypasses JWT validation and proceeds")
    void should_proceedWithoutValidation_when_signInPath() {
        when(chain.filter(any())).thenReturn(Mono.empty());
        var exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/api/v1/auth/sign-in").build());

        filter.filter(exchange, chain).block();

        verify(jwtTokenProvider, never()).validateToken(any());
        verify(chain).filter(any());
    }

    @Test
    @DisplayName("Protected path without Authorization header returns 401")
    void should_return401_when_authorizationHeaderMissing() {
        var exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/users/123").build());

        filter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(chain, never()).filter(any());
    }

    @Test
    @DisplayName("Protected path with non-Bearer Authorization header returns 401")
    void should_return401_when_authorizationHeaderIsNotBearer() {
        var exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/users/123")
                        .header("Authorization", "Basic dXNlcjpwYXNz")
                        .build());

        filter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(chain, never()).filter(any());
    }

    @Test
    @DisplayName("Protected path with expired or invalid JWT returns 401")
    void should_return401_when_jwtTokenIsInvalid() {
        doThrow(new InvalidJwtException("JWT token is expired"))
                .when(jwtTokenProvider).validateToken(any());

        var exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/users/123")
                        .header("Authorization", "Bearer expired.token.here")
                        .build());

        filter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(chain, never()).filter(any());
    }

    @Test
    @DisplayName("Protected path with valid JWT proceeds to the next filter")
    void should_proceedToNextFilter_when_jwtTokenIsValid() {
        when(chain.filter(any())).thenReturn(Mono.empty());
        doNothing().when(jwtTokenProvider).validateToken(any());

        var exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/users/123")
                        .header("Authorization", "Bearer valid.jwt.token")
                        .build());

        filter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode()).isNull();
        verify(chain).filter(any());
    }
}
