package com.platform.voice.service.stt;

import com.platform.voice.config.VoiceProperties;
import com.platform.voice.dto.UserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SttServiceFactory {

    private static final Logger log = LoggerFactory.getLogger(SttServiceFactory.class);

    private final List<SpeechToTextService> sttServices;
    private final VoiceProperties voiceProperties;

    public SttServiceFactory(List<SpeechToTextService> sttServices, VoiceProperties voiceProperties) {
        this.sttServices = sttServices;
        this.voiceProperties = voiceProperties;
    }

    public SpeechToTextService getSttService(UserContext context) {
        String configuredProvider = voiceProperties.stt().provider();
        String planType = context.planType();

        log.info("Selecting STT service: provider={}, plan={}", configuredProvider, planType);

        for (SpeechToTextService service : sttServices) {
            if (service.getProviderName().equalsIgnoreCase(configuredProvider)) {
                if (service.supportsPlan(planType)) {
                    log.info("Using STT provider: {} for plan: {}", service.getProviderName(), planType);
                    return service;
                }
            }
        }

        for (SpeechToTextService service : sttServices) {
            if (service.supportsPlan(planType)) {
                log.info("Falling back to STT provider: {} for plan: {}", service.getProviderName(), planType);
                return service;
            }
        }

        if (!sttServices.isEmpty()) {
            log.warn("No STT service found for plan {}, using first available", planType);
            return sttServices.get(0);
        }

        throw new RuntimeException("No STT service available");
    }
}