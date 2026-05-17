package com.platform.common.model;

import java.io.Serializable;

public record LearningContext(
    String targetLanguage,
    String targetLevel,
    String nativeLanguage
) implements Serializable {

    public LearningContext {
        if (targetLanguage == null || targetLanguage.isBlank()) {
            targetLanguage = "Czech";
        }
        if (targetLevel == null || targetLevel.isBlank()) {
            targetLevel = "A1";
        }
        if (nativeLanguage == null || nativeLanguage.isBlank()) {
            nativeLanguage = "en";
        }
    }

    public static LearningContext defaults() {
        return new LearningContext("Czech", "A1", "en");
    }

    public String toNamespace() {
        return targetLanguage.toLowerCase() + "_" + targetLevel.toUpperCase();
    }
}