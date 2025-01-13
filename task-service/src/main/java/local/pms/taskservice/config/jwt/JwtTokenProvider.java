package local.pms.taskservice.config.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;

import jakarta.annotation.PostConstruct;

import local.pms.taskservice.service.AWSSecretsManagerService;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.GrantedAuthority;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import org.springframework.stereotype.Component;

import java.security.PublicKey;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;

import java.security.spec.X509EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;

import java.util.Date;
import java.util.List;
import java.util.Base64;
import java.util.Collections;

import java.util.stream.Stream;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private static final String ALGORITHM = "RSA";
    private static final String PUBLIC_KEY = "project-management-system-public-key";

    private final AWSSecretsManagerService awsSecretsManagerService;

    private PublicKey publicKey;

    @PostConstruct
    public void init() {
        publicKey = loadPublicKey();
    }

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
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private PublicKey loadPublicKey() {
        String keyContent = cleanKey(awsSecretsManagerService.getKey(PUBLIC_KEY));
        byte[] decodedKey = Base64.getDecoder().decode(keyContent);
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
            return keyFactory.generatePublic(new X509EncodedKeySpec(decodedKey));
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