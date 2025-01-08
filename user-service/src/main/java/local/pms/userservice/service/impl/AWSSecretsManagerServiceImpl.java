package local.pms.userservice.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;

import com.fasterxml.jackson.databind.ObjectMapper;

import local.pms.userservice.service.AWSSecretsManagerService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import lombok.experimental.FieldDefaults;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Service;

import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;

import software.amazon.awssdk.services.secretsmanager.model.SecretsManagerException;

import java.util.Map;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class AWSSecretsManagerServiceImpl implements AWSSecretsManagerService {

    @Value("${aws.secretsmanager.secretName}")
    String secretName;

    final SecretsManagerClient secretsManagerClient;
    final ObjectMapper objectMapper;

    @Override
    public String getKey(String keyName) {
        return getSecrets().getOrDefault(keyName, null);
    }

    private Map<String, String> getSecrets() {
        String secret = fetchSecretValue();
        return parseSecretToMap(secret);
    }

    private String fetchSecretValue() {
        try {
            return secretsManagerClient.getSecretValue(request -> request.secretId(secretName)).secretString();
        } catch (SecretsManagerException e) {
            log.error("Failed to retrieve secret. Error: {}", e.awsErrorDetails());
            throw new RuntimeException("Failed to retrieve secret", e);
        }
    }

    private Map<String, String> parseSecretToMap(String secret) {
        try {
            return objectMapper.readValue(secret, new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Failed to parse secret. Error: {}", e.getMessage());
            throw new RuntimeException("Failed to parse secret", e);
        }
    }
}