package com.platform.voice.service.analytics;

import com.platform.voice.config.AnalyticsProperties;
import com.platform.voice.config.VoiceProperties;
import com.platform.voice.dto.UserContext;
import com.platform.voice.model.AnalysisResult;
import com.platform.voice.model.AnalysisResult.CEFRAlignment;
import com.platform.voice.model.AnalysisResult.IdentifiedMistake;
import com.platform.voice.model.AnalysisResult.ProgressMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.util.*;

@Service
public class LlmAnalyticsService implements AnalyticsStrategy {

    private static final Logger log = LoggerFactory.getLogger(LlmAnalyticsService.class);

    private final VoiceProperties voiceProperties;
    private final AnalyticsProperties analyticsProperties;
    private final WebClient webClient;
    private final Counter llmAnalysisCounter;
    private final Timer llmAnalysisTimer;

    public LlmAnalyticsService(
            VoiceProperties voiceProperties,
            AnalyticsProperties analyticsProperties,
            MeterRegistry meterRegistry) {
        this.voiceProperties = voiceProperties;
        this.analyticsProperties = analyticsProperties;
        this.webClient = WebClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com")
                .build();
        this.llmAnalysisCounter = Counter.builder("voice.analytics.llm.requests")
                .description("Number of LLM-powered analytics requests")
                .register(meterRegistry);
        this.llmAnalysisTimer = Timer.builder("voice.analytics.llm.duration")
                .description("Time to complete LLM analytics")
                .register(meterRegistry);
    }

    @Override
    public AnalysisResult analyze(
        String transcript,
        UserContext context,
        long audioDurationMs,
        double confidence
    ) {
        llmAnalysisCounter.increment();
        
        return llmAnalysisTimer.record(() -> doAnalyze(transcript, context, audioDurationMs, confidence));
    }

    private AnalysisResult doAnalyze(
        String transcript,
        UserContext context,
        long audioDurationMs,
        double confidence
    ) {
        String apiKey = voiceProperties.gemini().apiKey();
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("GEMINI_API_KEY not configured, returning basic analysis");
            return generateBasicAnalysis(transcript, context, audioDurationMs, confidence);
        }

        try {
            String prompt = buildAnalysisPrompt(transcript, context, audioDurationMs, confidence);
            String analysis = callGeminiApi(prompt, apiKey);
            
            return parseAnalysisResponse(analysis, transcript, context, audioDurationMs, confidence);
            
        } catch (Exception e) {
            log.error("LLM analysis failed: {}", e.getMessage());
            return generateBasicAnalysis(transcript, context, audioDurationMs, confidence);
        }
    }

    private String buildAnalysisPrompt(String transcript, UserContext context, long audioDurationMs, double confidence) {
        return String.format("""
            You are an expert Czech language teacher analyzing a student's speech.
            
            Student Profile:
            - Target Language: %s
            - Current Level: %s
            - Native Language: %s
            - Plan Type: %s
            
            Transcript to Analyze:
            "%s"
            
            Audio Duration: %d ms
            Transcription Confidence: %.2f
            
            Please analyze this transcript and return a JSON response with:
            1. identified_mistakes: Array of mistakes with type, description, severity, original_text, suggested_correction, category
            2. cefr_assessment: {suggested_level, a1_readiness (0-100), a2_readiness (0-100), b1_readiness (0-100), assessment_text}
            3. progress_metrics: {session_score (0-100), improvement_percent, total_sessions_so_far, audio_duration_ms}
            4. recommendations: Array of 3-5 specific improvement recommendations
            
            Return ONLY valid JSON, no markdown or extra text. Format:
            {
              "mistakes": [...],
              "cefr": {...},
              "progress": {...},
              "recommendations": [...]
            }
            """,
            context.targetLanguage(),
            context.targetLevel(),
            context.nativeLanguage(),
            context.planType(),
            transcript,
            audioDurationMs,
            confidence
        );
    }

    private String callGeminiApi(String prompt, String apiKey) {
        String modelName = analyticsProperties.llmModel();
        String apiUrl = String.format("/v1beta/models/%s:generateContent?key=%s", modelName, apiKey);

        Map<String, Object> requestBody = Map.of(
            "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))),
            "generationConfig", Map.of(
                "temperature", 0.3,
                "maxOutputTokens", 2048
            )
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
            throw new RuntimeException("Failed to call Gemini API", e);
        }
    }

    private String parseGeminiResponse(String response) {
        try {
            var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            var node = mapper.readTree(response);
            return node.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText("{}");
        } catch (Exception e) {
            log.warn("Failed to parse Gemini response: {}", e.getMessage());
            return "{}";
        }
    }

    private AnalysisResult parseAnalysisResponse(
        String analysisJson,
        String transcript,
        UserContext context,
        long audioDurationMs,
        double confidence
    ) {
        try {
            var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            var json = mapper.readTree(analysisJson);
            
            List<IdentifiedMistake> mistakes = new ArrayList<>();
            if (json.has("mistakes")) {
                for (var node : json.get("mistakes")) {
                    mistakes.add(new IdentifiedMistake(
                        node.path("type").asText("unknown"),
                        node.path("description").asText(""),
                        node.path("severity").asText("medium"),
                        node.path("original_text").asText(""),
                        node.path("suggested_correction").asText(""),
                        node.path("category").asText("general")
                    ));
                }
            }
            
            CEFRAlignment cefr = new CEFRAlignment(
                json.path("cefr").path("suggested_level").asText("A1"),
                json.path("cefr").path("a1_readiness").asInt(50),
                json.path("cefr").path("a2_readiness").asInt(20),
                json.path("cefr").path("b1_readiness").asInt(0),
                json.path("cefr").path("assessment_text").asText("")
            );
            
            ProgressMetrics progress = new ProgressMetrics(
                json.path("progress").path("session_score").asDouble(70.0),
                json.path("progress").path("improvement_percent").asDouble(0.0),
                json.path("progress").path("total_sessions").asLong(1L),
                audioDurationMs,
                Map.of()
            );
            
            List<String> recommendations = new ArrayList<>();
            if (json.has("recommendations")) {
                for (var node : json.get("recommendations")) {
                    recommendations.add(node.asText());
                }
            }
            
            return new AnalysisResult(
                UUID.randomUUID().toString(),
                "llm-powered",
                context.planType(),
                transcript,
                mistakes,
                progress,
                cefr,
                recommendations,
                Instant.now(),
                analyticsProperties.llmModel()
            );
            
        } catch (Exception e) {
            log.error("Failed to parse analysis JSON: {}", e.getMessage());
            return generateBasicAnalysis(transcript, context, audioDurationMs, confidence);
        }
    }

    private AnalysisResult generateBasicAnalysis(
        String transcript,
        UserContext context,
        long audioDurationMs,
        double confidence
    ) {
        log.info("Generating basic analysis for transcript: {}", transcript);
        
        List<IdentifiedMistake> mistakes = List.of();
        CEFRAlignment cefr = new CEFRAlignment(
            context.targetLevel(),
            50, 20, 0,
            "Basic assessment - LLM unavailable"
        );
        ProgressMetrics progress = new ProgressMetrics(
            70.0, 0.0, 1L, audioDurationMs, Map.of()
        );
        List<String> recommendations = List.of(
            "Practice basic greetings daily",
            "Focus on clear pronunciation",
            "Listen to native Czech speakers"
        );
        
        return new AnalysisResult(
            UUID.randomUUID().toString(),
            "llm-powered-fallback",
            context.planType(),
            transcript,
            mistakes,
            progress,
            cefr,
            recommendations,
            Instant.now(),
            "fallback-v1"
        );
    }

    @Override
    public String getAnalyticsType() {
        return "llm-powered";
    }

    @Override
    public boolean supportsPlan(String planType) {
        return "PREMIUM".equalsIgnoreCase(planType) || "ENTERPRISE".equalsIgnoreCase(planType);
    }
}