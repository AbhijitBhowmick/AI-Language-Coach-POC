package com.platform.profile.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserProfileResponse(
        UUID userId,
        String email,
        String targetLanguage,
        String targetLevel,
        String nativeLanguage,
        Double readinessScore,
        Boolean diagnosticCompleted,
        Long createdAt,
        Long updatedAt
) {
    public static UserProfileResponseBuilder builder() {
        return new UserProfileResponseBuilder();
    }

    public static class UserProfileResponseBuilder {
        private UUID userId;
        private String email;
        private String targetLanguage;
        private String targetLevel;
        private String nativeLanguage;
        private Double readinessScore;
        private Boolean diagnosticCompleted;
        private Long createdAt;
        private Long updatedAt;

        public UserProfileResponseBuilder userId(UUID userId) { this.userId = userId; return this; }
        public UserProfileResponseBuilder email(String email) { this.email = email; return this; }
        public UserProfileResponseBuilder targetLanguage(String targetLanguage) { this.targetLanguage = targetLanguage; return this; }
        public UserProfileResponseBuilder targetLevel(String targetLevel) { this.targetLevel = targetLevel; return this; }
        public UserProfileResponseBuilder nativeLanguage(String nativeLanguage) { this.nativeLanguage = nativeLanguage; return this; }
        public UserProfileResponseBuilder readinessScore(Double readinessScore) { this.readinessScore = readinessScore; return this; }
        public UserProfileResponseBuilder diagnosticCompleted(Boolean diagnosticCompleted) { this.diagnosticCompleted = diagnosticCompleted; return this; }
        public UserProfileResponseBuilder createdAt(Long createdAt) { this.createdAt = createdAt; return this; }
        public UserProfileResponseBuilder updatedAt(Long updatedAt) { this.updatedAt = updatedAt; return this; }

        public UserProfileResponse build() {
            return new UserProfileResponse(userId, email, targetLanguage, targetLevel, nativeLanguage, readinessScore, diagnosticCompleted, createdAt, updatedAt);
        }
    }

    public UUID getUserId() { return userId; }
    public String getEmail() { return email; }
    public String getTargetLanguage() { return targetLanguage; }
    public String getTargetLevel() { return targetLevel; }
    public String getNativeLanguage() { return nativeLanguage; }
    public Double getReadinessScore() { return readinessScore; }
    public Boolean getDiagnosticCompleted() { return diagnosticCompleted; }
    public Long getCreatedAt() { return createdAt; }
    public Long getUpdatedAt() { return updatedAt; }
}