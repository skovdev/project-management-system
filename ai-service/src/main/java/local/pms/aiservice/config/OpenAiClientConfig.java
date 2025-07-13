package local.pms.aiservice.config;

import com.openai.client.OpenAIClient;

import com.openai.client.okhttp.OpenAIOkHttpClient;

import local.pms.aiservice.service.aws.AwsSecretsManagerService;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class OpenAiClientConfig {

    private static final String PROJECT_MANAGEMENT_SYSTEM_OPENAI_API_KEY_SECRET_NAME = "project-management-system-openai-api-key";
    private static final String OPENAI_PUBLIC_API_KEY = "openai-public-api-key";

    @Value("${project-management-system.openai.chat-gpt.api-url}")
    private String apiUrl;

    private final AwsSecretsManagerService awsSecretsManagerService;

    @Bean
    public OpenAIClient openAIClient() {
        return OpenAIOkHttpClient.builder()
                .apiKey(takeOpenAiPublicApiKey())
                .build();
    }

    private String takeOpenAiPublicApiKey() {
        String apiKey = awsSecretsManagerService.getValueByKeyAndSecretName(OPENAI_PUBLIC_API_KEY, PROJECT_MANAGEMENT_SYSTEM_OPENAI_API_KEY_SECRET_NAME);
        if (apiKey == null || apiKey.isEmpty()) {
            log.error("OpenAI API key is not found. Please check your AWS Secrets Manager configuration");
            throw new IllegalStateException("OpenAI API key is not found in AWS Secrets Manager");
        }
        log.info("OpenAI API key successfully retrieved from AWS Secrets Manager");
        return apiKey;
    }
}
