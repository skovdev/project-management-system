package local.pms.apigateway.service;

public interface AwsSecretsManagerService {
    String getValueByKeyAndSecretName(String keyName, String secretName);
}
