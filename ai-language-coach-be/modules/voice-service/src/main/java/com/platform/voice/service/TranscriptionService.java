package com.platform.voice.service;

import com.platform.voice.dto.UserContext;
import com.platform.voice.model.AnalysisResult;
import com.platform.voice.model.TranscriptionRecord;
import com.platform.voice.model.UserProgress;
import com.platform.voice.service.analytics.AnalyticsFactory;
import com.platform.voice.service.progress.ProgressTracker;
import com.platform.voice.service.storage.TranscriptionStorageService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TranscriptionService {

    private static final Logger log = LoggerFactory.getLogger(TranscriptionService.class);

    private final TranscriptionStorageService storageService;
    private final AnalyticsFactory analyticsFactory;
    private final ProgressTracker progressTracker;
    private final Counter transcriptionCounter;
    private final Counter skippedCounter;

    public TranscriptionService(
            TranscriptionStorageService storageService,
            AnalyticsFactory analyticsFactory,
            ProgressTracker progressTracker,
            MeterRegistry meterRegistry) {
        this.storageService = storageService;
        this.analyticsFactory = analyticsFactory;
        this.progressTracker = progressTracker;
        this.transcriptionCounter = Counter.builder("voice.transcription.records")
                .description("Number of transcriptions recorded")
                .register(meterRegistry);
        this.skippedCounter = Counter.builder("voice.transcription.skipped")
                .description("Number of transcriptions skipped (FREE tier)")
                .register(meterRegistry);
    }

    public TranscriptionResult processTranscription(
            String sessionId,
            byte[] audioData,
            String transcript,
            UserContext context,
            double confidence,
            long audioDurationMs,
            String sttProvider
    ) {
        String planType = context.planType();

        if (!storageService.shouldRecord(planType)) {
            log.debug("Skipping recording for FREE tier user");
            skippedCounter.increment();
            return new TranscriptionResult(transcript, context, null, null);
        }

        TranscriptionRecord record = storageService.saveTranscription(
                sessionId,
                audioData,
                transcript,
                context,
                confidence,
                audioDurationMs,
                sttProvider
        );
        transcriptionCounter.increment();

        AnalysisResult analysis = analyticsFactory.analyze(
                transcript,
                context,
                audioDurationMs,
                confidence
        );

        UserProgress progress = progressTracker.updateProgress(context, transcript, analysis);

        log.info("Processed transcription for session: {}, user: {}, record: {}",
                sessionId, context.userId(), record.recordId());

        return new TranscriptionResult(transcript, context, record, analysis);
    }

    public UserProgress getUserProgress(UserContext context) {
        return progressTracker.getProgress(context);
    }

    public java.util.Map<String, Object> getProgressSummary(UserContext context) {
        return progressTracker.getProgressSummary(context);
    }

    public record TranscriptionResult(
            String transcript,
            UserContext context,
            TranscriptionRecord record,
            AnalysisResult analysis
    ) {}
}