package local.pms.apigateway.config.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;

import jakarta.annotation.PostConstruct;

import local.pms.apigateway.exception.InvalidJwtException;

import local.pms.apigateway.service.AwsSecretsManagerService;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Component;

import java.security.PublicKey;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;

import java.security.spec.X509EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;

import java.util.Date;
import java.util.List;
import java.util.Base64;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private static final String ALGORITHM = "RSA";
    private static final String PUBLIC_KEY_FIELD = "project-management-system-public-key";

    @Value("${aws.secretsmanager.secretName.project-management-system-security-private-public-keys}")
    private String secretName;

    private final AwsSecretsManagerService awsSecretsManagerService;

    private PublicKey publicKey;

    @PostConstruct
    public void init() {
        publicKey = loadPublicKey();
    }

    public boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }

    public void validateToken(String token) {
        try {
            var claims = extractClaims(token);
            if (claims.getExpiration().before(new Date())) {
                throw new InvalidJwtException("JWT token is expired");
            }
        } catch (JwtException e) {
            throw new InvalidJwtException("Invalid JWT token: " + e.getMessage(), e);
        }
    }

    public String extractUsername(String token) {
        return extractClaims(token).get("username", String.class);
    }

    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        var roles = extractClaims(token).get("roles");
        if (roles instanceof List<?> list) {
            return (List<String>) list;
        }
        return List.of();
    }

    private Claims extractClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException e) {
            throw new InvalidJwtException("JWT parsing failed: " + e.getMessage(), e);
        }
    }

    private PublicKey loadPublicKey() {
        var keyContent = cleanPemHeaders(
                awsSecretsManagerService.getValueByKeyAndSecretName(PUBLIC_KEY_FIELD, secretName));
        var decodedKey = Base64.getDecoder().decode(keyContent);
        try {
            var keyFactory = KeyFactory.getInstance(ALGORITHM);
            return keyFactory.generatePublic(new X509EncodedKeySpec(decodedKey));
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            log.error("Failed to load RSA public key: {}", e.getMessage(), e);
            throw new InvalidJwtException("Failed to load RSA public key", e);
        }
    }

    private String cleanPemHeaders(String keyContent) {
        return keyContent
                .replaceAll("-----BEGIN [A-Z ]+-----", "")
                .replaceAll("-----END [A-Z ]+-----", "")
                .replaceAll("\\s", "");
    }
}
