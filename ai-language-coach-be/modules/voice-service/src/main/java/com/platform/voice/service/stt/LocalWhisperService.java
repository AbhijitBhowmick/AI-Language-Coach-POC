package com.platform.voice.service.stt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.platform.voice.config.VoiceProperties;
import com.platform.voice.dto.TranscriptionResult;
import com.platform.voice.dto.UserContext;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;

@Service
public class LocalWhisperService implements SpeechToTextService {

    private static final Logger log = LoggerFactory.getLogger(LocalWhisperService.class);

    private final VoiceProperties voiceProperties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final Timer whisperTimer;

    public LocalWhisperService(VoiceProperties voiceProperties, MeterRegistry meterRegistry) {
        this.voiceProperties = voiceProperties;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        this.whisperTimer = Timer.builder("voice.stt.whisper.duration")
                .description("Time to transcribe audio with Whisper")
                .register(meterRegistry);
    }

    @Override
    public TranscriptionResult transcribe(byte[] audioData, UserContext context) {
        long startTime = System.currentTimeMillis();

        try {
            String base64Audio = Base64.getEncoder().encodeToString(audioData);

            WhisperRequest request = new WhisperRequest(
                base64Audio,
                voiceProperties.whisper().model(),
                context.targetLanguage()
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<WhisperRequest> entity = new HttpEntity<>(request, headers);

            String whisperUrl = voiceProperties.whisper().apiUrl() + "/transcriptions";

            ResponseEntity<WhisperResponse> response = restTemplate.postForEntity(
                    whisperUrl, entity, WhisperResponse.class);

            long processingTime = System.currentTimeMillis() - startTime;

            if (response.getBody() != null) {
                log.info("Whisper transcription completed in {}ms for user {}", processingTime, context.userId());
                return TranscriptionResult.of(
                        response.getBody().text(),
                        context.targetLanguage(),
                        processingTime,
                        "whisper"
                );
            }

            throw new RuntimeException("Empty response from Whisper API");

        } catch (Exception e) {
            log.error("Whisper transcription failed: {}", e.getMessage(), e);
            throw new RuntimeException("Whisper transcription failed: " + e.getMessage(), e);
        }
    }

    @Override
    public String getProviderName() {
        return "whisper";
    }

    @Override
    public boolean supportsPlan(String planType) {
        return "FREE".equalsIgnoreCase(planType);
    }

    private static record WhisperRequest(String audio, String model, String language) {}

    private static record WhisperResponse(String text) {}
}