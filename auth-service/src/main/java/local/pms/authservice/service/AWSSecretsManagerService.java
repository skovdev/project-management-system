package local.pms.authservice.service;

public interface AWSSecretsManagerService {
    String getValueByKeyAndSecretName(String keyName, String secretName);
}