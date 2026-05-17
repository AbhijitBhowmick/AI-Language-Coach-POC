package com.platform.voice.service.tts;

import com.platform.voice.config.VoiceProperties;
import com.platform.voice.dto.UserContext;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class GeminiTtsService implements TextToSpeechService {

    private static final Logger log = LoggerFactory.getLogger(GeminiTtsService.class);

    private final VoiceProperties voiceProperties;
    private final Timer ttsTimer;
    private final WebClient webClient;

    public GeminiTtsService(VoiceProperties voiceProperties, MeterRegistry meterRegistry) {
        this.voiceProperties = voiceProperties;
        this.ttsTimer = Timer.builder("voice.tts.duration")
                .description("Time to synthesize speech with Gemini")
                .register(meterRegistry);
        this.webClient = WebClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com")
                .build();
    }

    @Override
    public byte[] synthesize(String text, UserContext context) {
        log.info("Synthesizing TTS for user {} in {}", context.userId(), context.targetLanguage());
        return ttsTimer.record(() -> doSynthesize(text, context));
    }

    private byte[] doSynthesize(String text, UserContext context) {
        String apiKey = voiceProperties.gemini().apiKey();
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("GEMINI_API_KEY not configured, returning placeholder audio");
            return generatePlaceholderAudio(text);
        }

        try {
            String prompt = buildTtsPrompt(text, context);
            
            String response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v1beta/models/{model}:generateContent")
                            .queryParam("key", apiKey)
                            .build(voiceProperties.gemini().ttsModel()))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            String responseText = response != null ? response : generatePlaceholderAudioStr(text);
            return responseText.getBytes();

        } catch (Exception e) {
            log.warn("Gemini TTS API call failed, returning placeholder: {}", e.getMessage());
            return generatePlaceholderAudio(text);
        }
    }

    private String generatePlaceholderAudioStr(String text) {
        return "TTS placeholder for: " + text;
    }

    private byte[] generatePlaceholderAudio(String text) {
        return generatePlaceholderAudioStr(text).getBytes();
    }

    private String buildTtsPrompt(String text, UserContext context) {
        return String.format(
                "You are a language learning assistant for a student learning %s at level %s. " +
                "Your native language is %s. " +
                "Generate a natural, conversational response in %s to the following message. " +
                "Keep it brief and appropriate for a language lesson:\n\n%s",
                context.targetLanguage(),
                context.targetLevel(),
                context.nativeLanguage(),
                context.targetLanguage(),
                text
        );
    }

    @Override
    public String getProviderName() {
        return "gemini";
    }

    @Override
    public boolean supportsPlan(String planType) {
        return true;
    }
}