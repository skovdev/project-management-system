package local.pms.notificationservice.service;

/**
 * Retrieves secrets stored in AWS Secrets Manager.
 */
public interface AWSSecretsManagerService {

    /**
     * Returns the value for the given key within the named AWS secret.
     *
     * @param keyName    the JSON key inside the secret
     * @param secretName the AWS Secrets Manager secret name
     * @return the secret value, or {@code null} if the key is absent
     */
    String getValueByKeyAndSecretName(String keyName, String secretName);
}
