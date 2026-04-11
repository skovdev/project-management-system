package local.pms.apigateway.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;

import com.fasterxml.jackson.databind.ObjectMapper;

import local.pms.apigateway.exception.AwsSecretsRetrievalException;

import local.pms.apigateway.service.AwsSecretsManagerService;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;

import software.amazon.awssdk.services.secretsmanager.model.SecretsManagerException;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AwsSecretsManagerServiceImpl implements AwsSecretsManagerService {

    private final SecretsManagerClient secretsManagerClient;
    private final ObjectMapper objectMapper;

    @Override
    public String getValueByKeyAndSecretName(String keyName, String secretName) {
        return fetchAndParseSecret(secretName).getOrDefault(keyName, null);
    }

    private Map<String, String> fetchAndParseSecret(String secretName) {
        return parseSecretToMap(fetchSecretString(secretName));
    }

    private String fetchSecretString(String secretName) {
        try {
            return secretsManagerClient
                    .getSecretValue(request -> request.secretId(secretName))
                    .secretString();
        } catch (SecretsManagerException e) {
            log.error("Failed to retrieve secret '{}' from AWS: {}", secretName, e.awsErrorDetails(), e);
            throw new AwsSecretsRetrievalException("Failed to retrieve secret: " + secretName, e);
        }
    }

    private Map<String, String> parseSecretToMap(String secret) {
        try {
            return objectMapper.readValue(secret, new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Failed to parse secret JSON: {}", e.getMessage(), e);
            throw new AwsSecretsRetrievalException("Failed to parse secret JSON", e);
        }
    }
}
