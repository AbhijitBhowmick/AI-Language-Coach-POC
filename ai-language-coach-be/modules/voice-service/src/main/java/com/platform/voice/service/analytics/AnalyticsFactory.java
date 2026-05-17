package com.platform.voice.service.analytics;

import com.platform.voice.config.AnalyticsProperties;
import com.platform.voice.dto.UserContext;
import com.platform.voice.model.AnalysisResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnalyticsFactory {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsFactory.class);

    private final List<AnalyticsStrategy> strategies;
    private final AnalyticsProperties analyticsProperties;

    public AnalyticsFactory(List<AnalyticsStrategy> strategies, AnalyticsProperties analyticsProperties) {
        this.strategies = strategies;
        this.analyticsProperties = analyticsProperties;
    }

    public AnalyticsStrategy getStrategy(String planType) {
        log.debug("Selecting analytics strategy for plan: {}", planType);

        for (AnalyticsStrategy strategy : strategies) {
            if (strategy.supportsPlan(planType)) {
                log.info("Selected analytics strategy: {} for plan: {}", 
                    strategy.getAnalyticsType(), planType);
                return strategy;
            }
        }

        log.warn("No specific analytics strategy found for plan: {}, returning null", planType);
        return null;
    }

    public AnalysisResult analyze(
        String transcript,
        UserContext context,
        long audioDurationMs,
        double confidence
    ) {
        String planType = context.planType();

        if (!analyticsProperties.enabled()) {
            log.debug("Analytics disabled, skipping analysis");
            return null;
        }

        AnalyticsStrategy strategy = getStrategy(planType);
        if (strategy == null) {
            log.debug("No analytics strategy available for plan: {}", planType);
            return null;
        }

        try {
            return strategy.analyze(transcript, context, audioDurationMs, confidence);
        } catch (Exception e) {
            log.error("Analytics analysis failed: {}", e.getMessage(), e);
            return null;
        }
    }

    public boolean isRecordingEnabled(String planType) {
        return analyticsProperties.isRecordingEnabled(planType);
    }

    public String getAnalyticsType(String planType) {
        return analyticsProperties.getAnalyticsType(planType);
    }
}