package com.example.logcollector.config;

import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiProviderConfig {

    @Value("${langchain4j.open-ai.chat-model.api-key:demo}")
    private String openAiApiKey;

    @Value("${langchain4j.open-ai.chat-model.model-name:gpt-5.2}")
    private String openAiModelName;

    @Value("${langchain4j.google-ai-gemini.chat-model.api-key:demo}")
    private String geminiApiKey;

    @Value("${langchain4j.google-ai-gemini.chat-model.model-name:gemini-1.5-flash-001}")
    private String geminiModelName;

    @Value("${langchain4j.anthropic.chat-model.api-key:demo}")
    private String anthropicApiKey;

    @Value("${langchain4j.anthropic.chat-model.model-name:claude-3-haiku-20240307}")
    private String anthropicModelName;

    @Value("${langchain4j.ollama.chat-model.base-url:http://host.docker.internal:11434}")
    private String ollamaBaseUrl;

    @Value("${langchain4j.ollama.chat-model.model-name:llama3}")
    private String ollamaModelName;

    @Bean
    @ConditionalOnProperty(name = "ai.provider", havingValue = "openai", matchIfMissing = true)
    public ChatLanguageModel openAiChatModel() {
        // Ensure SSL bypass is active globally
        com.example.logcollector.util.SslTrustManagerHelper.trustAllCertificates();

        // Set additional JVM properties to disable SSL verification
        System.setProperty("javax.net.ssl.trustAll", "true");
        System.setProperty("jdk.tls.client.protocols", "TLSv1.2");

        return OpenAiChatModel.builder()
                .apiKey(openAiApiKey)
                .modelName(openAiModelName)
                .temperature(0.0)
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "ai.provider", havingValue = "gemini")
    public ChatLanguageModel geminiChatModel() {
        // Ensure SSL bypass is active globally
        com.example.logcollector.util.SslTrustManagerHelper.trustAllCertificates();

        return GoogleAiGeminiChatModel.builder()
                .apiKey(geminiApiKey)
                .modelName(geminiModelName)
                .temperature(0.0)
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "ai.provider", havingValue = "anthropic")
    public ChatLanguageModel anthropicChatModel() {
        // Ensure SSL bypass is active globally
        com.example.logcollector.util.SslTrustManagerHelper.trustAllCertificates();

        return AnthropicChatModel.builder()
                .apiKey(anthropicApiKey)
                .modelName(anthropicModelName)
                .temperature(0.0)
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "ai.provider", havingValue = "ollama")
    public ChatLanguageModel ollamaChatModel() {
        return OllamaChatModel.builder()
                .baseUrl(ollamaBaseUrl)
                .modelName(ollamaModelName)
                .timeout(java.time.Duration.ofMinutes(5))
                .temperature(0.0)
                .build();
    }
}
