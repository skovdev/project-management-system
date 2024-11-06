package local.pms.userservice.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;

import io.jsonwebtoken.security.Keys;

import lombok.AccessLevel;

import lombok.experimental.FieldDefaults;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.security.core.GrantedAuthority;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

import java.nio.charset.StandardCharsets;

import java.util.Date;
import java.util.List;
import java.util.Collections;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JwtUtil {

    @Value("${security.jwt.token.secret-key}")
    String secretKey;

    public boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }

    public String extractUsername(String token) {
        return extractClaims(token).get("username", String.class);
    }

    public List<GrantedAuthority> extractAuthorities(String token) {
        return Stream.concat(
                        extractRoles(token).stream(),
                        extractPermissions(token).stream())
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    public List<GrantedAuthority> extractRoles(String token) {
        List<String> roles = extractClaims(token).get("roles", List.class);
        return !roles.isEmpty() ? convertRolesToGrantedAuthority(roles) : Collections.emptyList();
    }

    private List<GrantedAuthority> convertRolesToGrantedAuthority(List<String> roles) {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    public List<GrantedAuthority> extractPermissions(String token) {
        List<String> permissions = extractClaims(token).get("permissions", List.class);
        return !permissions.isEmpty() ? convertPermissionsToGrantedAuthority(permissions) : Collections.emptyList();
    }

    private List<GrantedAuthority> convertPermissionsToGrantedAuthority(List<String> roles) {
        return roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignerKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSignerKey() {
        byte[] secretKeyBytes = secretKey.getBytes(StandardCharsets.UTF_16);
        return Keys.hmacShaKeyFor(secretKeyBytes);
    }
}