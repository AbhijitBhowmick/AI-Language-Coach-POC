package com.platform.voice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "voice.analytics")
public record AnalyticsProperties(
    boolean enabled,
    String freeTierAnalyticsType,
    String standardTierAnalyticsType,
    String premiumTierAnalyticsType,
    String llmModel
) {
    public AnalyticsProperties {
        if (enabled == false) enabled = true;
        if (freeTierAnalyticsType == null) freeTierAnalyticsType = "none";
        if (standardTierAnalyticsType == null) standardTierAnalyticsType = "rule-based";
        if (premiumTierAnalyticsType == null) premiumTierAnalyticsType = "llm-powered";
        if (llmModel == null) llmModel = "gemini-2.0-flash-exp";
    }

    public boolean isRecordingEnabled(String planType) {
        return "STANDARD".equalsIgnoreCase(planType) || "PREMIUM".equalsIgnoreCase(planType);
    }

    public String getAnalyticsType(String planType) {
        if ("FREE".equalsIgnoreCase(planType)) return freeTierAnalyticsType;
        if ("STANDARD".equalsIgnoreCase(planType)) return standardTierAnalyticsType;
        if ("PREMIUM".equalsIgnoreCase(planType)) return premiumTierAnalyticsType;
        return "none";
    }
}