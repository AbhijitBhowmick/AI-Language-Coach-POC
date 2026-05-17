package com.platform.profile.dto;

public record UpdateProfileRequest(
        String targetLanguage,
        String targetLevel,
        String nativeLanguage
) {
    public String getTargetLanguage() { return targetLanguage; }
    public String getTargetLevel() { return targetLevel; }
    public String getNativeLanguage() { return nativeLanguage; }
}