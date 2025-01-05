package local.pms.authservice.service;

public interface AWSSecretsManagerService {
    String getKey(String keyName);
}