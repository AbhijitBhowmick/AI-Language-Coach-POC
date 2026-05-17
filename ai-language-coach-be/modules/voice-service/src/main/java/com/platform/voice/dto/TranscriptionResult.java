package com.platform.voice.dto;

public record TranscriptionResult(
    String text,
    String language,
    double confidence,
    long processingTimeMs,
    String provider
) {
    public static TranscriptionResult of(String text, String language, long processingTimeMs, String provider) {
        return new TranscriptionResult(text, language, 1.0, processingTimeMs, provider);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String text;
        private String language;
        private double confidence = 1.0;
        private long processingTimeMs;
        private String provider;

        public Builder text(String text) { this.text = text; return this; }
        public Builder language(String language) { this.language = language; return this; }
        public Builder confidence(double confidence) { this.confidence = confidence; return this; }
        public Builder processingTimeMs(long processingTimeMs) { this.processingTimeMs = processingTimeMs; return this; }
        public Builder provider(String provider) { this.provider = provider; return this; }

        public TranscriptionResult build() {
            return new TranscriptionResult(text, language, confidence, processingTimeMs, provider);
        }
    }
}