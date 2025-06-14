package local.pms.projectservice.service;

public interface AWSSecretsManagerService {
    String getValueByKeyAndSecretName(String keyName, String secretName);
}