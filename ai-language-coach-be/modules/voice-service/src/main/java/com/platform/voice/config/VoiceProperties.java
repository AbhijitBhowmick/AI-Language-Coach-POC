package com.platform.voice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "voice")
public record VoiceProperties(
    SttProperties stt,
    TtsProperties tts,
    WhisperProperties whisper,
    GeminiProperties gemini,
    ClaudeProperties claude,
    OpenAiProperties openai,
    ConversationProperties conversation,
    ProfileServiceProperties profileService
) {
    public VoiceProperties {
        if (stt == null) stt = new SttProperties("gemini");
        if (tts == null) tts = new TtsProperties("gemini");
        if (whisper == null) whisper = new WhisperProperties(null, null, 30);
        if (gemini == null) gemini = new GeminiProperties(null, null, null, null);
        if (claude == null) claude = new ClaudeProperties(null, null);
        if (openai == null) openai = new OpenAiProperties(null, null, null, null);
        if (conversation == null) conversation = new ConversationProperties(10, "conversation:");
        if (profileService == null) profileService = new ProfileServiceProperties(null, 5);
    }

    public SttProperties stt() { return stt; }
    public TtsProperties tts() { return tts; }
    public WhisperProperties whisper() { return whisper; }
    public GeminiProperties gemini() { return gemini; }
    public ClaudeProperties claude() { return claude; }
    public OpenAiProperties openai() { return openai; }
    public ConversationProperties conversation() { return conversation; }
    public ProfileServiceProperties profileService() { return profileService; }

    public record SttProperties(String provider) {
        public SttProperties { if (provider == null) provider = "gemini"; }
    }

    public record TtsProperties(String provider) {
        public TtsProperties { if (provider == null) provider = "gemini"; }
    }

    public record WhisperProperties(String apiUrl, String model, int timeoutSeconds) {
        public WhisperProperties {
            if (apiUrl == null) apiUrl = "http://localhost:8001/v1";
            if (model == null) model = "base";
            if (timeoutSeconds == 0) timeoutSeconds = 30;
        }
    }

    public record GeminiProperties(String apiKey, String model, String sttModel, String ttsModel) {
        public GeminiProperties {
            if (model == null) model = "gemini-2.0-flash-exp";
            if (sttModel == null) sttModel = "gemini-2.0-flash-exp";
            if (ttsModel == null) ttsModel = "gemini-2.0-flash-exp";
        }
    }

    public record ClaudeProperties(String apiKey, String model) {
        public ClaudeProperties {
            if (model == null) model = "claude-3-opus-20240229";
        }
    }

    public record OpenAiProperties(String apiKey, String model, String sttModel, String ttsModel) {
        public OpenAiProperties {
            if (model == null) model = "gpt-4-turbo";
            if (sttModel == null) sttModel = "whisper-1";
            if (ttsModel == null) ttsModel = "tts-1";
        }
    }

    public record ConversationProperties(long ttlMinutes, String prefix) {
        public ConversationProperties {
            if (ttlMinutes == 0) ttlMinutes = 10;
            if (prefix == null) prefix = "conversation:";
        }
    }

    public record ProfileServiceProperties(String baseUrl, int timeoutSeconds) {
        public ProfileServiceProperties {
            if (baseUrl == null) baseUrl = "http://localhost:8082";
            if (timeoutSeconds == 0) timeoutSeconds = 5;
        }
    }
}