package local.pms.userservice.config.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;

import jakarta.annotation.PostConstruct;

import local.pms.userservice.service.AWSSecretsManagerService;

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
import java.util.UUID;
import java.util.Base64;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private static final String ALGORITHM = "RSA";
    private static final String PUBLIC_KEY = "project-management-system-public-key";

    @Value("${aws.secretsmanager.secretName.project-management-system-security-private-public-keys}")
    private String privatePublicKeysSecretName;

    private final AWSSecretsManagerService awsSecretsManagerService;

    private PublicKey publicKey;

    @PostConstruct
    public void init() {
        publicKey = loadPublicKey();
    }

    public UUID extractAuthUserId(String token) {
        return UUID.fromString(extractClaims(token).get("authUserId", String.class));
    }

    public boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }

    public String extractUsername(String token) {
        return extractClaims(token).get("username", String.class);
    }

    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private PublicKey loadPublicKey() {
        String keyContent = cleanKey(awsSecretsManagerService.getValueByKeyAndSecretName(PUBLIC_KEY, privatePublicKeysSecretName));
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