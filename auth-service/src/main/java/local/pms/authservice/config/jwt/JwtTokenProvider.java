package local.pms.authservice.config.jwt;

import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;

import jakarta.annotation.PostConstruct;

import jakarta.servlet.http.HttpServletRequest;

import local.pms.authservice.exception.InvalidJwtAuthenticationException;

import local.pms.authservice.service.AWSSecretsManagerService;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.core.Authentication;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.stereotype.Component;

import java.security.Key;
import java.security.PublicKey;
import java.security.PrivateKey;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;

import java.security.spec.X509EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;

import java.util.Map;
import java.util.Date;
import java.util.Base64;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private static final String PRIVATE_KEY = "project-management-system-private-key";
    private static final String PUBLIC_KEY = "project-management-system-public-key";

    private PrivateKey privateKey;
    private PublicKey publicKey;

    private final UserDetailsService userDetailsService;
    private final AWSSecretsManagerService awsSecretsManagerService;

    @PostConstruct
    public void init() {
        privateKey = loadKey(PRIVATE_KEY, "RSA", true);
        publicKey = loadKey(PUBLIC_KEY, "RSA", false);
    }

    public String createToken(Map<String, Object> data) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + 3600000);
        return Jwts.builder()
                .claims(data).issuedAt(now).expiration(validity)
                .signWith(privateKey)
                .compact();
    }

    public Authentication authenticate(String token) {
        try {
            UserDetails userDetails = userDetailsService.loadUserByUsername(extractUsername(token));
            return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
        } catch (UsernameNotFoundException e) {
            throw new UsernameNotFoundException("User not found");
        }
    }

    public String extractUsername(String token) {
        return parseSignedClaims(token)
                .getPayload()
                .get("username", String.class);
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
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token);
    }

    @SuppressWarnings("unchecked")
    private <T extends Key> T loadKey(String key, String algorithm, boolean isPrivate) {
        String keyContent = cleanKey(awsSecretsManagerService.getKey(key));
        byte[] decodedKey = Base64.getDecoder().decode(keyContent);
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
            return isPrivate
                    ? (T) keyFactory.generatePrivate(new PKCS8EncodedKeySpec(decodedKey))
                    : (T) keyFactory.generatePublic(new X509EncodedKeySpec(decodedKey));
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            log.error("Failed to load key. Error: {}", e.getMessage());
            throw new RuntimeException("Failed to load key", e);
        }
    }

    private String cleanKey(String keyContent) {
        return keyContent
                .replaceAll("-----BEGIN [A-Z ]+-----", "")
                .replaceAll("-----END [A-Z ]+-----", "")
                .replaceAll("\\s", "");
    }
}