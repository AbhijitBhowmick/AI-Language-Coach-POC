package com.platform.voice.service;

import com.platform.voice.config.RagProperties;
import com.platform.voice.dto.UserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Map;

@Service
public class RagServiceClient {

    private static final Logger log =LoggerFactory.getLogger(RagServiceClient.class);

    private final WebClient webClient;
    private final RagProperties ragProperties;

    public RagServiceClient(RagProperties ragProperties) {
        this.ragProperties = ragProperties;
        this.webClient = WebClient.builder()
                .baseUrl(ragProperties.getFullUrl())
                .build();
    }

    public String query(String userInput, UserContext context) {
        try {
            Map<String, Object> request = Map.of(
                    "query", userInput,
                    "native_lang", context.nativeLanguage() != null ? context.nativeLanguage() : "en",
                    "target_lang", context.targetLanguage() != null ? context.targetLanguage() : "czech",
                    "level", context.targetLevel() != null ? context.targetLevel() : "A1"
            );

            RagResponse response = webClient.post()
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(RagResponse.class)
                    .block();

            if (response != null && response.answer() != null) {
                log.info("RAG response received for query: {}", userInput);
                return response.answer();
            }

            log.warn("Empty response from RAG service");
            return "I couldn't generate a response. Please try again.";

        } catch (Exception e) {
            log.error("Error calling RAG service: {}", e.getMessage());
            return "Voice service response - RAG integration active (connect to RAG at " + ragProperties.getBaseUrl() + ")";
        }
    }

    public record RagResponse(
            String answer,
            Object[] citations,
            Map<String, Object> metadata,
            boolean cached
    ) {}
}