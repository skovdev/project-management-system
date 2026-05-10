package local.pms.apigateway.config.security;

import local.pms.apigateway.config.jwt.JwtTokenProvider;

import local.pms.apigateway.exception.InvalidJwtException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.mock.http.server.reactive.MockServerHttpRequest;

import org.springframework.mock.web.server.MockServerWebExchange;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.doNothing;

@ExtendWith(MockitoExtension.class)
class JwtServerSecurityContextRepositoryTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    private JwtServerSecurityContextRepository repository;

    @BeforeEach
    void setUp() {
        repository = new JwtServerSecurityContextRepository(jwtTokenProvider);
    }

    @Test
    @DisplayName("load() returns populated SecurityContext for a valid Bearer token")
    void should_returnPopulatedContext_when_validBearerToken() {
        doNothing().when(jwtTokenProvider).validateToken("valid.jwt");
        when(jwtTokenProvider.extractUsername("valid.jwt")).thenReturn("alice");
        when(jwtTokenProvider.extractRoles("valid.jwt")).thenReturn(List.of("ROLE_USER"));

        var exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/users/1")
                        .header("Authorization", "Bearer valid.jwt")
                        .build());

        var context = repository.load(exchange).block();

        assertThat(context).isNotNull();
        assertThat(context.getAuthentication()).isInstanceOf(UsernamePasswordAuthenticationToken.class);
        assertThat(context.getAuthentication().getName()).isEqualTo("alice");
        assertThat(context.getAuthentication().getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_USER");
    }

    @Test
    @DisplayName("load() returns empty Mono when Authorization header is absent")
    void should_returnEmpty_when_authorizationHeaderMissing() {
        var exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/users/1").build());

        var result = repository.load(exchange).blockOptional();

        assertThat(result).isEmpty();
        verify(jwtTokenProvider, never()).validateToken(any());
    }

    @Test
    @DisplayName("load() returns empty Mono when Authorization header is not Bearer")
    void should_returnEmpty_when_authorizationHeaderIsNotBearer() {
        var exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/users/1")
                        .header("Authorization", "Basic dXNlcjpwYXNz")
                        .build());

        var result = repository.load(exchange).blockOptional();

        assertThat(result).isEmpty();
        verify(jwtTokenProvider, never()).validateToken(any());
    }

    @Test
    @DisplayName("load() returns empty Mono when JWT validation throws InvalidJwtException")
    void should_returnEmpty_when_jwtTokenIsInvalid() {
        doThrow(new InvalidJwtException("JWT token is expired"))
                .when(jwtTokenProvider).validateToken("expired.jwt");

        var exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/users/1")
                        .header("Authorization", "Bearer expired.jwt")
                        .build());

        var result = repository.load(exchange).blockOptional();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("load() grants multiple roles when JWT contains several role claims")
    void should_returnContextWithMultipleRoles_when_tokenHasMultipleRoles() {
        doNothing().when(jwtTokenProvider).validateToken("multi.jwt");
        when(jwtTokenProvider.extractUsername("multi.jwt")).thenReturn("admin");
        when(jwtTokenProvider.extractRoles("multi.jwt")).thenReturn(List.of("ROLE_USER", "ROLE_ADMIN"));

        var exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/users/1")
                        .header("Authorization", "Bearer multi.jwt")
                        .build());

        var context = repository.load(exchange).block();

        assertThat(context).isNotNull();
        assertThat(context.getAuthentication().getAuthorities())
                .extracting("authority")
                .containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
    }

    @Test
    @DisplayName("save() always returns empty Mono (stateless)")
    void should_returnEmpty_when_save() {
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/any").build());

        var result = repository.save(exchange, null).blockOptional();

        assertThat(result).isEmpty();
    }
}
