package local.pms.aiservice.config;

import local.pms.aiservice.service.aws.AwsSecretsManagerService;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.ai.chat.client.ChatClient;

import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;

import org.springframework.ai.openai.api.OpenAiApi;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures the Spring AI {@link ChatClient} using an OpenAI API key retrieved from
 * AWS Secrets Manager. Declaring {@link OpenAiChatModel} as a bean causes Spring AI's
 * {@code @ConditionalOnMissingBean(OpenAiChatModel.class)} to skip auto-configuration,
 * ensuring only the AWS-backed model is used.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class AiClientConfig {

    private static final String SECRET_NAME = "project-management-system-openai-api-key";
    private static final String KEY_NAME = "openai-public-api-key";

    private final AwsSecretsManagerService awsSecretsManagerService;

    @Value("${spring.ai.openai.chat.options.model:gpt-4.1-2025-04-14}")
    private String model;

    /**
     * Creates an {@link OpenAiChatModel} backed by an API key from AWS Secrets Manager
     * and the model configured via {@code spring.ai.openai.chat.options.model}.
     */
    @Bean
    public OpenAiChatModel openAiChatModel() {
        var openAiApi = OpenAiApi.builder()
                .apiKey(retrieveApiKey())
                .build();
        var options = OpenAiChatOptions.builder()
                .model(model)
                .build();
        return OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(options)
                .build();
    }

    /**
     * Builds the {@link ChatClient} from the auto-configured builder,
     * which uses the {@link OpenAiChatModel} bean above.
     */
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder.build();
    }

    private String retrieveApiKey() {
        String apiKey = awsSecretsManagerService.getValueByKeyAndSecretName(KEY_NAME, SECRET_NAME);
        if (apiKey == null || apiKey.isBlank()) {
            log.error("OpenAI API key is not found. Please check your AWS Secrets Manager configuration");
            throw new IllegalStateException("OpenAI API key is not found in AWS Secrets Manager");
        }
        log.info("OpenAI API key successfully retrieved from AWS Secrets Manager");
        return apiKey;
    }
}
