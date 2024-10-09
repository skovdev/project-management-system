package local.pms.authservice.config.jwt;

import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;

import io.jsonwebtoken.security.Keys;

import jakarta.servlet.http.HttpServletRequest;

import local.pms.authservice.exception.InvalidJwtAuthenticationException;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import lombok.experimental.FieldDefaults;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.core.Authentication;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

import java.nio.charset.StandardCharsets;

import java.util.Map;
import java.util.Date;
import java.util.Optional;

import java.util.stream.Collectors;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${security.jwt.token.secret-key}")
    String secretKey;

    @Value("${security.jwt.token.expire-length}")
    long validityInMilliseconds;

    final UserDetailsService userDetailsService;

    public String createToken(Map<String, Object> data) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);
        return Jwts.builder()
                .claims(data).issuedAt(now).expiration(validity)
                .signWith(getSecretKey())
                .compact();
    }

    public Authentication getAuthentication(String token) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(extractUsername(token));
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    public String extractUsername(String token) {
        return parseSignedClaims(token)
                .getPayload()
                .entrySet()
                .stream()
                .filter(entry -> entry.getKey().equals("username"))
                .map(entry -> String.valueOf(entry.getValue()))
                .collect(Collectors.joining());
    }

    public String resolveToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader("Authorization"))
                .filter(header -> header.startsWith("Bearer "))
                .map(header -> header.substring(7))
                .orElse(null);
    }

    public boolean validateToken(String token) {
        try {
            Jws<Claims> claimsJws = parseSignedClaims(token);
            return !claimsJws.getPayload().getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidJwtAuthenticationException("Expired or invalid JWT token");
        }
    }

    private Jws<Claims> parseSignedClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token);
    }

    private SecretKey getSecretKey() {
        byte[] secretKeyBytes = secretKey.getBytes(StandardCharsets.UTF_16);
        return Keys.hmacShaKeyFor(secretKeyBytes);
    }
}
