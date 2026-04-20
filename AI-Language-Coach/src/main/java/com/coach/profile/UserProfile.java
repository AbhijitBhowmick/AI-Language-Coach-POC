package com.coach.profile;

import com.coach.common.LearningContext;
import com.coach.common.config.ConfigService;

import java.io.Serializable;
import java.util.UUID;

public class UserProfile implements Serializable {
    private UUID userId;
    private String email;
    private LearningContext context;
    private PlanType planType;
    private double readinessScore;
    private boolean diagnosticCompleted;
    private long createdAt;
    private long updatedAt;

    public UserProfile() {}

    public UserProfile(UUID userId, String email, LearningContext context, PlanType planType,
                     double readinessScore, boolean diagnosticCompleted, long createdAt, long updatedAt) {
        this.userId = userId;
        this.email = email;
        this.context = context;
        this.planType = planType;
        this.readinessScore = readinessScore;
        this.diagnosticCompleted = diagnosticCompleted;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static UserProfileBuilder builder() {
        return new UserProfileBuilder();
    }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public LearningContext getContext() { return context; }
    public void setContext(LearningContext context) { this.context = context; }
    public PlanType getPlanType() { return planType; }
    public void setPlanType(PlanType planType) { this.planType = planType; }
    public double getReadinessScore() { return readinessScore; }
    public void setReadinessScore(double readinessScore) { this.readinessScore = readinessScore; }
    public boolean isDiagnosticCompleted() { return diagnosticCompleted; }
    public void setDiagnosticCompleted(boolean diagnosticCompleted) { this.diagnosticCompleted = diagnosticCompleted; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    public String getTargetLanguage() {
        return context != null && context.targetLanguage() != null ? context.targetLanguage() : "Czech";
    }

    public String getTargetLevel() {
        return context != null && context.targetLevel() != null ? context.targetLevel() : "A1";
    }

    public String getNativeLanguage() {
        return context != null && context.nativeLanguage() != null ? context.nativeLanguage() : "en";
    }

    public static class UserProfileBuilder {
        private UUID userId;
        private String email;
        private LearningContext context;
        private PlanType planType = PlanType.FREE;
        private double readinessScore;
        private boolean diagnosticCompleted;
        private long createdAt;
        private long updatedAt;

        public UserProfileBuilder() {}

        public UserProfileBuilder context(LearningContext context) { this.context = context; return this; }
        public UserProfileBuilder userId(UUID userId) { this.userId = userId; return this; }
        public UserProfileBuilder email(String email) { this.email = email; return this; }
        public UserProfileBuilder targetLanguage(String targetLanguage) {
            this.context = new LearningContext(targetLanguage, 
                this.context != null ? this.context.targetLevel() : "A1",
                this.context != null ? this.context.nativeLanguage() : "en");
            return this;
        }
        public UserProfileBuilder targetLevel(String targetLevel) {
            this.context = new LearningContext(
                this.context != null ? this.context.targetLanguage() : "Czech",
                targetLevel,
                this.context != null ? this.context.nativeLanguage() : "en");
            return this;
        }
        public UserProfileBuilder nativeLanguage(String nativeLanguage) {
            this.context = new LearningContext(
                this.context != null ? this.context.targetLanguage() : "Czech",
                this.context != null ? this.context.targetLevel() : "A1",
                nativeLanguage);
            return this;
        }
        public UserProfileBuilder planType(PlanType planType) { this.planType = planType; return this; }
        public UserProfileBuilder readinessScore(double readinessScore) { this.readinessScore = readinessScore; return this; }
        public UserProfileBuilder diagnosticCompleted(boolean diagnosticCompleted) { this.diagnosticCompleted = diagnosticCompleted; return this; }
        public UserProfileBuilder createdAt(long createdAt) { this.createdAt = createdAt; return this; }
        public UserProfileBuilder updatedAt(long updatedAt) { this.updatedAt = updatedAt; return this; }
        public UserProfile build() {
            if (context == null) {
                context = new LearningContext("Czech", "A1", "en");
            }
            return new UserProfile(userId, email, context, planType, readinessScore, diagnosticCompleted, createdAt, updatedAt);
        }
    }
}