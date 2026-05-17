package com.platform.voice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record VoiceMessage(
    MessageType type,
    String content,
    String userId,
    String sessionId,
    String targetLanguage,
    String targetLevel,
    String planType,
    Long timestamp
) {
    public enum MessageType {
        AUDIO, TEXT, START_SESSION, END_SESSION, HEARTBEAT, ERROR, TRANSCRIPTION, RESPONSE, TTS_READY
    }

    public static VoiceMessage of(MessageType type, String content) {
        return new VoiceMessage(type, content, null, null, null, null, null, null);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private MessageType type;
        private String content;
        private String userId;
        private String sessionId;
        private String targetLanguage;
        private String targetLevel;
        private String planType;
        private Long timestamp;

        public Builder type(MessageType type) { this.type = type; return this; }
        public Builder content(String content) { this.content = content; return this; }
        public Builder userId(String userId) { this.userId = userId; return this; }
        public Builder sessionId(String sessionId) { this.sessionId = sessionId; return this; }
        public Builder targetLanguage(String targetLanguage) { this.targetLanguage = targetLanguage; return this; }
        public Builder targetLevel(String targetLevel) { this.targetLevel = targetLevel; return this; }
        public Builder planType(String planType) { this.planType = planType; return this; }
        public Builder timestamp(Long timestamp) { this.timestamp = timestamp; return this; }

        public VoiceMessage build() {
            return new VoiceMessage(type, content, userId, sessionId, targetLanguage, targetLevel, planType, timestamp);
        }
    }
}