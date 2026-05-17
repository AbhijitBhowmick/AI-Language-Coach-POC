package com.platform.voice.service.stt;

import com.platform.voice.config.VoiceProperties;
import com.platform.voice.dto.TranscriptionResult;
import com.platform.voice.dto.UserContext;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Base64;
import java.util.Map;

@Service
public class GeminiVoiceService implements SpeechToTextService {

    private static final Logger log = LoggerFactory.getLogger(GeminiVoiceService.class);

    private final VoiceProperties voiceProperties;
    private final CircuitBreaker circuitBreaker;
    private final Timer geminiTimer;
    private final WebClient webClient;

    public GeminiVoiceService(VoiceProperties voiceProperties,
                              CircuitBreakerRegistry circuitBreakerRegistry,
                              MeterRegistry meterRegistry) {
        this.voiceProperties = voiceProperties;
        this.circuitBreaker = circuitBreakerRegistry.circuitBreaker("gemini-stt");
        this.geminiTimer = Timer.builder("voice.stt.gemini.duration")
                .description("Time to transcribe audio with Gemini")
                .register(meterRegistry);
        this.webClient = WebClient.builder().build();
    }

    @Override
    public TranscriptionResult transcribe(byte[] audioData, UserContext context) {
        long startTime = System.currentTimeMillis();

        return circuitBreaker.executeSupplier(() -> {
            try {
                String base64Audio = Base64.getEncoder().encodeToString(audioData);
                String prompt = buildPrompt(context);

                String result = geminiTimer.record(() -> {
                    return callGeminiApi(base64Audio, prompt);
                });

                long processingTime = System.currentTimeMillis() - startTime;

                log.info("Gemini STT completed in {}ms for user {}", processingTime, context.userId());

                return TranscriptionResult.of(result, context.targetLanguage(), processingTime, "gemini");

            } catch (Exception e) {
                log.error("Gemini STT failed: {}", e.getMessage(), e);
                throw new RuntimeException("Gemini STT failed: " + e.getMessage(), e);
            }
        });
    }

    private String callGeminiApi(String base64Audio, String prompt) {
        String apiKey = voiceProperties.gemini().apiKey();
        if (apiKey == null || apiKey.isBlank()) {
            throw new RuntimeException("GEMINI_API_KEY not configured");
        }

        String modelName = voiceProperties.gemini().sttModel();
        String apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/" + modelName + ":generateContent?key=" + apiKey;

        Map<String, Object> requestBody = Map.of(
            "contents", new Object[]{
                Map.of("parts", new Object[]{
                    Map.of("text", prompt),
                    Map.of("text", "Please transcribe the following audio: " + base64Audio)
                })
            }
        );

        try {
            String response = webClient.post()
                    .uri(apiUrl)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return parseGeminiResponse(response);

        } catch (Exception e) {
            log.error("Gemini API call failed: {}", e.getMessage());
            throw new RuntimeException("Failed to call Gemini API: " + e.getMessage(), e);
        }
    }

    private String parseGeminiResponse(String response) {
        try {
            var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            var node = mapper.readTree(response);
            return node.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText("Transcription placeholder");
        } catch (Exception e) {
            log.warn("Failed to parse Gemini response: {}", e.getMessage());
            return "Audio transcription: " + response.substring(0, Math.min(100, response.length()));
        }
    }

    private String buildPrompt(UserContext context) {
        return String.format(
                "You are a language learning assistant. The user is learning %s at level %s. " +
                "Their native language is %s. " +
                "Please transcribe the following audio accurately.",
                context.targetLanguage(),
                context.targetLevel(),
                context.nativeLanguage()
        );
    }

    @Override
    public String getProviderName() {
        return "gemini";
    }

    @Override
    public boolean supportsPlan(String planType) {
        return "STANDARD".equalsIgnoreCase(planType) ||
               "PREMIUM".equalsIgnoreCase(planType) ||
               "ENTERPRISE".equalsIgnoreCase(planType);
    }
}