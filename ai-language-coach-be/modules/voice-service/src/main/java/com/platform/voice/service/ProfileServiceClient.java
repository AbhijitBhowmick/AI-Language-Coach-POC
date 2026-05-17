package com.platform.voice.service;

import com.platform.voice.config.VoiceProperties;
import com.platform.voice.dto.UserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class ProfileServiceClient {

    private static final Logger log = LoggerFactory.getLogger(ProfileServiceClient.class);

    private final RestTemplate restTemplate;
    private final VoiceProperties voiceProperties;

    public ProfileServiceClient(VoiceProperties voiceProperties) {
        this.voiceProperties = voiceProperties;
        this.restTemplate = new RestTemplate();
    }

    public UserContext fetchUserProfile(String userId, String token) {
        try {
            String url = voiceProperties.profileService().baseUrl() + "/profile";

            var response = restTemplate.getForObject(url, Map.class);

            if (response != null) {
                String targetLanguage = getStringValue(response.get("targetLanguage"));
                String targetLevel = getStringValue(response.get("currentLevel"));
                String nativeLanguage = getStringValue(response.get("nativeLanguage"));
                String planType = getStringValue(response.get("planType"));

                return UserContext.builder()
                        .userId(userId)
                        .targetLanguage(targetLanguage != null ? targetLanguage : "Czech")
                        .targetLevel(targetLevel != null ? targetLevel : "A1")
                        .nativeLanguage(nativeLanguage != null ? nativeLanguage : "en")
                        .planType(planType != null ? planType : "FREE")
                        .build();
            }

        } catch (Exception e) {
            log.warn("Failed to fetch profile from profile-service: {}, using defaults", e.getMessage());
        }

        return UserContext.builder()
                .userId(userId)
                .targetLanguage("Czech")
                .targetLevel("A1")
                .nativeLanguage("en")
                .planType("FREE")
                .build();
    }

    private String getStringValue(Object value) {
        if (value == null) return null;
        return value.toString();
    }
}