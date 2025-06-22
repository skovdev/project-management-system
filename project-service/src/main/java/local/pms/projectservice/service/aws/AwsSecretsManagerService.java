package local.pms.projectservice.service.aws;

public interface AwsSecretsManagerService {
    String getValueByKeyAndSecretName(String keyName, String secretName);
}