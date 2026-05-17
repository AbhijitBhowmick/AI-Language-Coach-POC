package com.platform.voice.model;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public record UserProgress(
    String userIdHash,
    String targetLanguage,
    String targetLevel,
    long totalSessions,
    long totalTranscriptions,
    long totalAudioDurationMs,
    List<String> commonMistakes,
    Map<String, Long> wordFrequency,
    LevelProgress levelProgress,
    Instant lastSessionAt,
    Instant createdAt
) implements Serializable {

    public record LevelProgress(
        String currentLevel,
        int a1Score,
        int a2Score,
        int b1Score,
        int b2Score,
        long sessionsAtLevel
    ) implements Serializable {}

    public static UserProgress create(String userIdHash, String targetLanguage, String targetLevel) {
        return new UserProgress(
            userIdHash,
            targetLanguage,
            targetLevel,
            0L,
            0L,
            0L,
            List.of(),
            Map.of(),
            new LevelProgress(targetLevel, 0, 0, 0, 0, 0),
            Instant.now(),
            Instant.now()
        );
    }

    public UserProgress incrementSession(String newLevel) {
        return new UserProgress(
            userIdHash,
            targetLanguage,
            newLevel != null ? newLevel : targetLevel,
            totalSessions + 1,
            totalTranscriptions,
            totalAudioDurationMs,
            commonMistakes,
            wordFrequency,
            levelProgress,
            Instant.now(),
            createdAt
        );
    }
}