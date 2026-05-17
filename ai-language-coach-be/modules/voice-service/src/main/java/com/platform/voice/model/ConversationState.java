package com.platform.voice.model;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class ConversationState implements Serializable {

    private String userId;
    private String tenantId;
    private String planType;
    private String targetLanguage;
    private String targetLevel;
    private String nativeLanguage;
    private List<ConversationMessage> messages = new ArrayList<>();
    private Instant createdAt;
    private Instant lastActivityAt;
    private int messageCount;

    public ConversationState() {}

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getPlanType() { return planType; }
    public void setPlanType(String planType) { this.planType = planType; }
    public String getTargetLanguage() { return targetLanguage; }
    public void setTargetLanguage(String targetLanguage) { this.targetLanguage = targetLanguage; }
    public String getTargetLevel() { return targetLevel; }
    public void setTargetLevel(String targetLevel) { this.targetLevel = targetLevel; }
    public String getNativeLanguage() { return nativeLanguage; }
    public void setNativeLanguage(String nativeLanguage) { this.nativeLanguage = nativeLanguage; }
    public List<ConversationMessage> getMessages() { return messages; }
    public void setMessages(List<ConversationMessage> messages) { this.messages = messages; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getLastActivityAt() { return lastActivityAt; }
    public void setLastActivityAt(Instant lastActivityAt) { this.lastActivityAt = lastActivityAt; }
    public int getMessageCount() { return messageCount; }
    public void setMessageCount(int messageCount) { this.messageCount = messageCount; }

    public void addMessage(String role, String content) {
        if (this.messages == null) this.messages = new ArrayList<>();
        ConversationMessage msg = new ConversationMessage();
        msg.setRole(role);
        msg.setContent(content);
        msg.setTimestamp(Instant.now());
        this.messages.add(msg);
        this.messageCount++;
        this.lastActivityAt = Instant.now();
    }

    public void recordAudioProcessed() {
        if (!this.messages.isEmpty()) {
            this.messages.get(this.messages.size() - 1).setAudioProcessed(true);
        }
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String userId;
        private String tenantId;
        private String planType;
        private String targetLanguage;
        private String targetLevel;
        private String nativeLanguage;
        private List<ConversationMessage> messages = new ArrayList<>();
        private Instant createdAt;
        private Instant lastActivityAt;
        private int messageCount;

        public Builder userId(String userId) { this.userId = userId; return this; }
        public Builder tenantId(String tenantId) { this.tenantId = tenantId; return this; }
        public Builder planType(String planType) { this.planType = planType; return this; }
        public Builder targetLanguage(String targetLanguage) { this.targetLanguage = targetLanguage; return this; }
        public Builder targetLevel(String targetLevel) { this.targetLevel = targetLevel; return this; }
        public Builder nativeLanguage(String nativeLanguage) { this.nativeLanguage = nativeLanguage; return this; }
        public Builder messages(List<ConversationMessage> messages) { this.messages = messages; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
        public Builder lastActivityAt(Instant lastActivityAt) { this.lastActivityAt = lastActivityAt; return this; }
        public Builder messageCount(int messageCount) { this.messageCount = messageCount; return this; }
        public ConversationState build() {
            ConversationState state = new ConversationState();
            state.setUserId(userId);
            state.setTenantId(tenantId);
            state.setPlanType(planType);
            state.setTargetLanguage(targetLanguage);
            state.setTargetLevel(targetLevel);
            state.setNativeLanguage(nativeLanguage);
            state.setMessages(messages != null ? messages : new ArrayList<>());
            state.setCreatedAt(createdAt);
            state.setLastActivityAt(lastActivityAt);
            state.setMessageCount(messageCount);
            return state;
        }
    }

    public static class ConversationMessage implements Serializable {
        private String role;
        private String content;
        private Instant timestamp;
        private boolean audioProcessed;

        public ConversationMessage() {}

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public Instant getTimestamp() { return timestamp; }
        public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
        public boolean isAudioProcessed() { return audioProcessed; }
        public void setAudioProcessed(boolean audioProcessed) { this.audioProcessed = audioProcessed; }

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private String role;
            private String content;
            private Instant timestamp;
            private boolean audioProcessed;

            public Builder role(String role) { this.role = role; return this; }
            public Builder content(String content) { this.content = content; return this; }
            public Builder timestamp(Instant timestamp) { this.timestamp = timestamp; return this; }
            public Builder audioProcessed(boolean audioProcessed) { this.audioProcessed = audioProcessed; return this; }
            public ConversationMessage build() {
                ConversationMessage msg = new ConversationMessage();
                msg.setRole(role);
                msg.setContent(content);
                msg.setTimestamp(timestamp);
                msg.setAudioProcessed(audioProcessed);
                return msg;
            }
        }
    }
}