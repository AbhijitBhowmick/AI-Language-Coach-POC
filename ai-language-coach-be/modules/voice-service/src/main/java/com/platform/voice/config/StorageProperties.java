package com.platform.voice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "voice.storage")
public record StorageProperties(
    String basePath,
    TranscriptionProperties transcription,
    RetentionProperties retention
) {
    public StorageProperties {
        if (basePath == null) basePath = "/data/recordings";
        if (transcription == null) transcription = new TranscriptionProperties(true, true);
        if (retention == null) retention = new RetentionProperties(0, 30, 90);
    }

    public record TranscriptionProperties(
        boolean enabled,
        boolean anonymize
    ) {
        public TranscriptionProperties {
            if (enabled) enabled = true;
            if (anonymize) anonymize = true;
        }
    }

    public record RetentionProperties(
        int free,
        int standard,
        int premium
    ) {
        public RetentionProperties {
            if (free < 0) free = 0;
            if (standard < 0) standard = 30;
            if (premium < 0) premium = 90;
        }
    }
}