package com.platform.voice.service;

import com.platform.voice.config.VoiceProperties;
import com.platform.voice.dto.UserContext;
import com.platform.voice.model.ConversationState;
import com.platform.voice.service.stt.SttServiceFactory;
import com.platform.voice.service.tts.TextToSpeechService;
import com.platform.voice.dto.TranscriptionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Service
public class ConversationService {

    private static final Logger log = LoggerFactory.getLogger(ConversationService.class);

    private final RedisTemplate<String, Object> valkeyTemplate;
    private final VoiceProperties voiceProperties;
    private final SttServiceFactory sttServiceFactory;
    private final List<TextToSpeechService> ttsServices;
    private final RagServiceClient ragServiceClient;

    public ConversationService(
            RedisTemplate<String, Object> valkeyTemplate,
            VoiceProperties voiceProperties,
            SttServiceFactory sttServiceFactory,
            List<TextToSpeechService> ttsServices,
            RagServiceClient ragServiceClient) {
        this.valkeyTemplate = valkeyTemplate;
        this.voiceProperties = voiceProperties;
        this.sttServiceFactory = sttServiceFactory;
        this.ttsServices = ttsServices;
        this.ragServiceClient = ragServiceClient;
    }

    public ConversationState startSession(UserContext context) {
        String sessionId = generateSessionId(context.userId());

        ConversationState state = ConversationState.builder()
                .userId(context.userId())
                .tenantId(context.tenantId())
                .planType(context.planType())
                .targetLanguage(context.targetLanguage())
                .targetLevel(context.targetLevel())
                .nativeLanguage(context.nativeLanguage())
                .createdAt(java.time.Instant.now())
                .lastActivityAt(java.time.Instant.now())
                .messageCount(0)
                .build();

        String key = voiceProperties.conversation().prefix() + sessionId;
        Duration ttl = Duration.ofMinutes(voiceProperties.conversation().ttlMinutes());

        valkeyTemplate.opsForValue().set(key, state, ttl);

        log.info("Started conversation session {} for user {}", sessionId, context.userId());

        return state;
    }

    public Optional<ConversationState> getSession(String sessionId) {
        String key = voiceProperties.conversation().prefix() + sessionId;
        Object value = valkeyTemplate.opsForValue().get(key);

        if (value instanceof ConversationState state) {
            return Optional.of(state);
        }

        return Optional.empty();
    }

    public ConversationState processAudio(byte[] audioData, UserContext context) {
        String sessionId = generateSessionId(context.userId());

        ConversationState state = getSession(sessionId)
                .orElseGet(() -> startSession(context));

        var sttService = sttServiceFactory.getSttService(context);

        TranscriptionResult transcription = sttService.transcribe(audioData, context);

        state.addMessage("user", transcription.text());

        String key = voiceProperties.conversation().prefix() + sessionId;
        Duration ttl = Duration.ofMinutes(voiceProperties.conversation().ttlMinutes());
        valkeyTemplate.opsForValue().set(key, state, ttl);

        log.info("Processed audio for session {}, transcribed: {}", sessionId, transcription.text());

        return state;
    }

    public String processText(String text, UserContext context) {
        String sessionId = generateSessionId(context.userId());

        ConversationState state = getSession(sessionId)
                .orElseGet(() -> startSession(context));

        state.addMessage("user", text);

        String responseText = generateResponse(text, state);

        state.addMessage("assistant", responseText);

        String key = voiceProperties.conversation().prefix() + sessionId;
        Duration ttl = Duration.ofMinutes(voiceProperties.conversation().ttlMinutes());
        valkeyTemplate.opsForValue().set(key, state, ttl);

        log.info("Processed text for session {}, response: {}", sessionId, responseText);

        return responseText;
    }

    public byte[] synthesizeResponse(String text, UserContext context) {
        TextToSpeechService ttsService = ttsServices.stream()
                .filter(tts -> tts.supportsPlan(context.planType()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No TTS service available for plan: " + context.planType()));

        return ttsService.synthesize(text, context);
    }

    public void endSession(String sessionId) {
        String key = voiceProperties.conversation().prefix() + sessionId;
        valkeyTemplate.delete(key);
        log.info("Ended conversation session {}", sessionId);
    }

    public void keepAlive(String sessionId) {
        String key = voiceProperties.conversation().prefix() + sessionId;
        Duration ttl = Duration.ofMinutes(voiceProperties.conversation().ttlMinutes());
        valkeyTemplate.expire(key, ttl);
    }

    private String generateSessionId(String userId) {
        return userId + "_" + System.currentTimeMillis();
    }

    private String generateResponse(String userInput, ConversationState state) {
        try {
            UserContext context = new UserContext(
                    state.getUserId(),
                    state.getTenantId(),
                    state.getPlanType(),
                    state.getTargetLanguage(),
                    state.getTargetLevel(),
                    state.getNativeLanguage(),
                    null
            );
            return ragServiceClient.query(userInput, context);
        } catch (Exception e) {
            log.error("RAG query failed, using fallback: {}", e.getMessage());
            return "Echo: " + userInput + " (Voice service response)";
        }
    }
}