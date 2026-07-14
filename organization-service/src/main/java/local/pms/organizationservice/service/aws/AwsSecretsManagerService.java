package local.pms.organizationservice.service.aws;

public interface AwsSecretsManagerService {
    String getValueByKeyAndSecretName(String keyName, String secretName);
}
