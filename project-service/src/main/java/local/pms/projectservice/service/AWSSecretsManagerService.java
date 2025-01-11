package local.pms.projectservice.service;

public interface AWSSecretsManagerService {
    String getKey(String keyName);
}