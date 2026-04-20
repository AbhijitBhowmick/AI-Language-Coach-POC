package com.coach.common;

import com.coach.common.config.ConfigService;
import java.io.Serializable;

public record LearningContext(
    String targetLanguage,
    String targetLevel,
    String nativeLanguage
) implements Serializable {
    
    public LearningContext(ConfigService config) {
        this(
            config.getDefaultTargetLanguage(),
            config.getDefaultTargetLevel(),
            config.getDefaultNativeLanguage()
        );
    }

    public LearningContext(String targetLanguage, String targetLevel, String nativeLanguage) {
        this.targetLanguage = targetLanguage != null && !targetLanguage.isBlank() ? targetLanguage : "Czech";
        this.targetLevel = targetLevel != null && !targetLevel.isBlank() ? targetLevel : "A1";
        this.nativeLanguage = nativeLanguage != null && !nativeLanguage.isBlank() ? nativeLanguage : "en";
    }

    public static LearningContext defaultContext(ConfigService config) {
        return new LearningContext(
            config.getDefaultTargetLanguage(),
            config.getDefaultTargetLevel(),
            config.getDefaultNativeLanguage()
        );
    }

    public String toNamespace() {
        return targetLanguage.toLowerCase() + "_" + targetLevel.toUpperCase();
    }

    public String toRagNamespace() {
        return targetLanguage.toLowerCase() + "_" + targetLevel.toLowerCase();
    }
}