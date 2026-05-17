package com.platform.voice.service.analytics;

import com.platform.voice.dto.UserContext;
import com.platform.voice.model.AnalysisResult;

public interface AnalyticsStrategy {
    AnalysisResult analyze(
        String transcript,
        UserContext context,
        long audioDurationMs,
        double confidence
    );

    String getAnalyticsType();

    boolean supportsPlan(String planType);
}