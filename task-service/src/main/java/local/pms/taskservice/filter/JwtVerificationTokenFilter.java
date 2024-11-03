package local.pms.taskservice.filter;

import io.jsonwebtoken.MalformedJwtException;

import jakarta.servlet.FilterChain;

import jakarta.servlet.ServletException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import local.pms.taskservice.service.TokenService;

import local.pms.taskservice.util.JwtUtil;

import lombok.AccessLevel;

import lombok.RequiredArgsConstructor;

import lombok.experimental.FieldDefaults;

import lombok.extern.slf4j.Slf4j;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.core.GrantedAuthority;

import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Component;

import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import java.util.List;

@Slf4j
@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class JwtVerificationTokenFilter extends OncePerRequestFilter {

    final TokenService tokenService;
    final JwtUtil jwtUtil;

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
        if (jwtUtil.isTokenExpired(token)) {
            handleErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "JWT token is expired.");
            return;
        }
        SecurityContextHolder.getContext().setAuthentication(getJwtAuthenticationToken(token));
        tokenService.setToken(token);
        chain.doFilter(request, response);
    }

    private UsernamePasswordAuthenticationToken getJwtAuthenticationToken(String bearerToken) {
        String username = jwtUtil.extractUsername(bearerToken);
        List<GrantedAuthority> authorities = jwtUtil.extractAuthorities(bearerToken);
        return new UsernamePasswordAuthenticationToken(username, null, authorities);
    }

    private void handleErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.sendError(status, message);
    }
}
