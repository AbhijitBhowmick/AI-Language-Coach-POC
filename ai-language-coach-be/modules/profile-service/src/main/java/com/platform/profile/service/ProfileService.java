package com.platform.profile.service;

import com.platform.profile.dto.*;
import com.platform.profile.entity.UserProfile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
public class ProfileService {

    private static final String PROFILE_PREFIX = "profile_v2:";
    private static final Duration PROFILE_TTL = Duration.ofDays(365);

    private final RedisTemplate<String, Object> valkeyTemplate;
    private final com.platform.common.config.AppProperties appProperties;

    public ProfileService(RedisTemplate<String, Object> valkeyTemplate,
                          com.platform.common.config.AppProperties appProperties) {
        this.valkeyTemplate = valkeyTemplate;
        this.appProperties = appProperties;
    }

    public UserProfileResponse createProfile(UUID userId, String email, CreateProfileRequest request) {
        UserProfile profile = UserProfile.builder()
                .userId(userId)
                .email(email)
                .targetLanguage(request.getTargetLanguage() != null ? request.getTargetLanguage() : appProperties.getTargetLanguage())
                .targetLevel(request.getTargetLevel() != null ? request.getTargetLevel() : appProperties.getTargetLevel())
                .nativeLanguage(request.getNativeLanguage() != null ? request.getNativeLanguage() : appProperties.getNativeLanguage())
                .readinessScore(0.0)
                .diagnosticCompleted(false)
                .createdAt(System.currentTimeMillis())
                .updatedAt(System.currentTimeMillis())
                .build();

        valkeyTemplate.opsForValue().set(PROFILE_PREFIX + userId, profile, PROFILE_TTL);
        return toResponse(profile);
    }

    public UserProfileResponse getProfile(UUID userId) {
        Object result = valkeyTemplate.opsForValue().get(PROFILE_PREFIX + userId);
        if (result instanceof UserProfile profile) {
            return toResponse(profile);
        }
        return null;
    }

    public UserProfileResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        UserProfileResponse existing = getProfile(userId);
        if (existing == null) {
            throw new RuntimeException("Profile not found");
        }

        UserProfile profile = UserProfile.builder()
                .userId(userId)
                .email(existing.getEmail())
                .targetLanguage(request.getTargetLanguage() != null ? request.getTargetLanguage() : existing.getTargetLanguage())
                .targetLevel(request.getTargetLevel() != null ? request.getTargetLevel() : existing.getTargetLevel())
                .nativeLanguage(request.getNativeLanguage() != null ? request.getNativeLanguage() : existing.getNativeLanguage())
                .readinessScore(existing.getReadinessScore())
                .diagnosticCompleted(existing.getDiagnosticCompleted())
                .createdAt(existing.getCreatedAt())
                .updatedAt(System.currentTimeMillis())
                .build();

        valkeyTemplate.opsForValue().set(PROFILE_PREFIX + userId, profile, PROFILE_TTL);
        return toResponse(profile);
    }

    public void deleteProfile(UUID userId) {
        valkeyTemplate.delete(PROFILE_PREFIX + userId);
    }

    private UserProfileResponse toResponse(UserProfile profile) {
        return UserProfileResponse.builder()
                .userId(profile.getUserId())
                .email(profile.getEmail())
                .targetLanguage(profile.getTargetLanguage())
                .targetLevel(profile.getTargetLevel())
                .nativeLanguage(profile.getNativeLanguage())
                .readinessScore(profile.getReadinessScore())
                .diagnosticCompleted(profile.isDiagnosticCompleted())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }
}