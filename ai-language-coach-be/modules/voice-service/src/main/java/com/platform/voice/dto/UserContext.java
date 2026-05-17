package com.platform.voice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserContext(
    String userId,
    String tenantId,
    String planType,
    String targetLanguage,
    String targetLevel,
    String nativeLanguage,
    String email
) {
    public static UserContext of(String userId, String tenantId) {
        return new UserContext(userId, tenantId, "FREE", "Czech", "A1", "en", null);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String userId;
        private String tenantId;
        private String planType;
        private String targetLanguage;
        private String targetLevel;
        private String nativeLanguage;
        private String email;

        public Builder userId(String userId) { this.userId = userId; return this; }
        public Builder tenantId(String tenantId) { this.tenantId = tenantId; return this; }
        public Builder planType(String planType) { this.planType = planType; return this; }
        public Builder targetLanguage(String targetLanguage) { this.targetLanguage = targetLanguage; return this; }
        public Builder targetLevel(String targetLevel) { this.targetLevel = targetLevel; return this; }
        public Builder nativeLanguage(String nativeLanguage) { this.nativeLanguage = nativeLanguage; return this; }
        public Builder email(String email) { this.email = email; return this; }

        public UserContext build() {
            return new UserContext(userId, tenantId, planType, targetLanguage, targetLevel, nativeLanguage, email);
        }
    }
}