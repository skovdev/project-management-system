package local.pms.aiservice.service.aws;

public interface AwsSecretsManagerService {
    String getValueByKeyAndSecretName(String keyName, String secretName);
}
