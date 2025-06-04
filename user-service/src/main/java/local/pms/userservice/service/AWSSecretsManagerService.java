package local.pms.userservice.service;

public interface AWSSecretsManagerService {
    String getValueByKeyAndSecretName(String keyName, String secretName);
}