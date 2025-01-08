package local.pms.userservice.service;

public interface AWSSecretsManagerService {
    String getKey(String keyName);
}