package local.pms.userservice.config.aws;

import lombok.AccessLevel;

import lombok.experimental.FieldDefaults;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;

import software.amazon.awssdk.regions.Region;

import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;

@Configuration
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AWSSecretsManagerConfig {

    @Value("${aws.iam.user.accessKey}")
    String accessKey;

    @Value("${aws.iam.user.secretKey}")
    String secretKey;

    @Bean
    public SecretsManagerClient secretsManagerClient() {
        return SecretsManagerClient.builder()
                .region(Region.EU_NORTH_1)
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(
                        accessKey, secretKey)))
                .build();
    }
}
