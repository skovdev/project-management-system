package local.pms.apigateway.config.properties;

import jakarta.validation.constraints.NotBlank;

import org.springframework.boot.context.properties.ConfigurationProperties;

import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "aws.iam.user")
public record AwsProperties(
        @NotBlank String accessKey,
        @NotBlank String secretKey
) {}
