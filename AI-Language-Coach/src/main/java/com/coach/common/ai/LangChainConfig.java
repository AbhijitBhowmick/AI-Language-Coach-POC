package com.coach.common.ai;

import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LangChainConfig {

    @Value("${langchain4j.google-ai-gemini.api-key}")
    private String geminiApiKey;

    @Bean
    public GoogleAiGeminiChatModel geminiFlashModel() {
        return GoogleAiGeminiChatModel.builder()
                .apiKey(geminiApiKey)
                .modelName("gemini-1.5-flash")
                .temperature(0.7)
                .maxRetries(3)
                .build();
    }

    @Bean
    public GoogleAiGeminiChatModel geminiProModel() {
        return GoogleAiGeminiChatModel.builder()
                .apiKey(geminiApiKey)
                .modelName("gemini-1.5-pro")
                .temperature(0.5)
                .maxRetries(3)
                .build();
    }
}
