package local.pms.apigateway.config.aws;

import local.pms.apigateway.config.properties.AwsProperties;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;

import software.amazon.awssdk.regions.Region;

import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;

/**
 * Configures the AWS Secrets Manager client used to retrieve the JWT RSA public key.
 *
 * <p>Credentials are read from {@link AwsProperties}, which is bound from
 * the {@code aws.iam.user.*} configuration prefix.
 */
@Configuration
@RequiredArgsConstructor
public class AwsSecretsManagerConfig {

    private final AwsProperties awsProperties;

    /**
     * Creates a {@link SecretsManagerClient} scoped to the EU_NORTH_1 region
     * using IAM credentials supplied via configuration.
     */
    @Bean
    public SecretsManagerClient secretsManagerClient() {
        return SecretsManagerClient.builder()
                .region(Region.EU_NORTH_1)
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(
                                awsProperties.accessKey(),
                                awsProperties.secretKey())))
                .build();
    }
}
