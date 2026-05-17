package com.platform.voice.model;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public record AnalysisResult(
    String recordId,
    String analysisType,
    String planType,
    String transcript,
    List<IdentifiedMistake> mistakes,
    ProgressMetrics progress,
    CEFRAlignment cefrAlignment,
    List<String> recommendations,
    Instant analyzedAt,
    String modelVersion
) implements Serializable {

    public record IdentifiedMistake(
        String type,
        String description,
        String severity,
        String originalText,
        String suggestedCorrection,
        String category
    ) implements Serializable {}

    public record ProgressMetrics(
        double sessionScore,
        double improvementPercent,
        long totalSessions,
        long audioDurationMs,
        Map<String, Long> topicFrequency
    ) implements Serializable {}

    public record CEFRAlignment(
        String suggestedLevel,
        int a1Readiness,
        int a2Readiness,
        int b1Readiness,
        String assessment
    ) implements Serializable {}
}