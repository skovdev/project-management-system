package local.pms.aiservice.service;

public interface AWSSecretsManagerService {
    String getValueByKeyAndSecretName(String keyName, String secretName);
}
