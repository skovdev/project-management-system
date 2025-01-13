package local.pms.taskservice.service;

public interface AWSSecretsManagerService {
    String getKey(String keyName);
}