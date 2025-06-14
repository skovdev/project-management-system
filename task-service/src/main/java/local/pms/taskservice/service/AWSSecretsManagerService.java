package local.pms.taskservice.service;

public interface AWSSecretsManagerService {
    String getValueByKeyAndSecretName(String keyName, String secretName);
}