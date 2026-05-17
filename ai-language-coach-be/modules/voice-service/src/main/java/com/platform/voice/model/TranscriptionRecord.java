package com.platform.voice.model;

import java.io.Serializable;
import java.time.Instant;

public record TranscriptionRecord(
    String recordId,
    String sessionId,
    String userIdHash,
    String tenantIdHash,
    String targetLanguage,
    String targetLevel,
    String provider,
    String transcript,
    double confidence,
    long audioDurationMs,
    String planType,
    Instant timestamp,
    String audioFilePath,
    String checksum,
    Metadata metadata
) implements Serializable {

    public record Metadata(
        String fileFormat,
        long fileSizeBytes,
        String originalFileName,
        String userAgent,
        String sessionDurationMs
    ) implements Serializable {}
}