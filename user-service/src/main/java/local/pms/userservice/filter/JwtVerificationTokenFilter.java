package local.pms.userservice.filter;

import io.jsonwebtoken.MalformedJwtException;

import jakarta.servlet.FilterChain;

import jakarta.servlet.ServletException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import local.pms.userservice.service.TokenService;

import local.pms.userservice.config.jwt.JwtTokenProvider;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import lombok.experimental.FieldDefaults;

import lombok.extern.slf4j.Slf4j;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.core.GrantedAuthority;

import org.springframework.security.core.context.SecurityContextHolder;


import org.springframework.stereotype.Component;

import org.springframework.util.AntPathMatcher;

import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import java.util.List;

@Slf4j
@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class JwtVerificationTokenFilter extends OncePerRequestFilter {

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    private static final List<String> PUBLIC_PATHS = List.of("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html");

    final JwtTokenProvider jwtTokenProvider;
    final TokenService tokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException {

        if (isAuthMissing(request)) {
            handleErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Authentication information is missing.");
            return;
        }

        String bearerToken = getBearerToken(request);

        if (bearerToken == null) {
            handleErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Bearer token is missing.");
            return;
        }

        try {
            validateTokenAndProceed(request, response, chain, bearerToken);
        } catch (MalformedJwtException e) {
            handleErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "JWT token is malformed.");
        } catch (Exception e) {
            handleErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, "Authentication failed due to an internal error. Please try again.");
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return PUBLIC_PATHS.stream().anyMatch(publicPath -> PATH_MATCHER.match(publicPath, path));
    }

    private boolean isAuthMissing(HttpServletRequest request) {
        return request.getHeader("Authorization") == null;
    }

    private String getBearerToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    private void validateTokenAndProceed(HttpServletRequest request, HttpServletResponse response, FilterChain chain, String token) throws IOException, ServletException {
        if (jwtTokenProvider.isTokenExpired(token)) {
            handleErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "JWT token is expired.");
            return;
        }
        tokenService.setToken(token);
        SecurityContextHolder.getContext().setAuthentication(fillAuthenticationToken(token));
        chain.doFilter(request, response);
    }

    private UsernamePasswordAuthenticationToken fillAuthenticationToken(String bearerToken) {
        String username = jwtTokenProvider.extractUsername(bearerToken);
        List<GrantedAuthority> authorities = jwtTokenProvider.extractAuthorities(bearerToken);
        return new UsernamePasswordAuthenticationToken(username, null, authorities);
    }

    private void handleErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.sendError(status, message);
    }
}
