package com.coach.profile;

import com.coach.common.LearningContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
public class ProfileService {

    private static final String PROFILE_PREFIX = "profile:";
    private static final Duration PROFILE_TTL = Duration.ofDays(365);
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ProfileService.class);

    private final RedisTemplate<String, Object> valkeyTemplate;

    public ProfileService(RedisTemplate<String, Object> valkeyTemplate) {
        this.valkeyTemplate = valkeyTemplate;
    }

    public UserProfile createProfile(UUID userId, String email, LearningContext context, PlanType planType) {
        UserProfile profile = UserProfile.builder()
                .userId(userId)
                .email(email)
                .context(context)
                .planType(planType)
                .readinessScore(0.0)
                .diagnosticCompleted(false)
                .createdAt(System.currentTimeMillis())
                .updatedAt(System.currentTimeMillis())
                .build();

        String key = PROFILE_PREFIX + userId.toString();
        valkeyTemplate.opsForValue().set(key, profile, PROFILE_TTL);
        log.info("Created profile for user: {} with context: {}", userId, context);
        
        return profile;
    }

    public UserProfile getProfile(UUID userId) {
        String key = PROFILE_PREFIX + userId.toString();
        Object result = valkeyTemplate.opsForValue().get(key);
        
        if (result instanceof UserProfile) {
            return (UserProfile) result;
        }
        
        return null;
    }

    public UserProfile updateProfile(UUID userId, ProfileUpdateRequest request) {
        UserProfile profile = getProfile(userId);
        
        if (profile == null) {
            throw new RuntimeException("Profile not found for user: " + userId);
        }

        LearningContext currentContext = profile.getContext();
        
        if (request.getTargetLanguage() != null || request.getTargetLevel() != null || request.getNativeLanguage() != null) {
            LearningContext newContext = new LearningContext(
                request.getTargetLanguage() != null ? request.getTargetLanguage() : currentContext.targetLanguage(),
                request.getTargetLevel() != null ? request.getTargetLevel() : currentContext.targetLevel(),
                request.getNativeLanguage() != null ? request.getNativeLanguage() : currentContext.nativeLanguage()
            );
            profile.setContext(newContext);
        }
        
        if (request.getPlanType() != null) {
            profile.setPlanType(request.getPlanType());
        }
        
        profile.setUpdatedAt(System.currentTimeMillis());
        
        String key = PROFILE_PREFIX + userId.toString();
        valkeyTemplate.opsForValue().set(key, profile, PROFILE_TTL);
        log.info("Updated profile for user: {}", userId);
        
        return profile;
    }

    public UserProfile updateReadinessScore(UUID userId, double score, LearningContext context) {
        UserProfile profile = getProfile(userId);
        
        if (profile == null) {
            throw new RuntimeException("Profile not found for user: " + userId);
        }

        profile.setReadinessScore(score);
        profile.setContext(context);
        profile.setDiagnosticCompleted(true);
        profile.setUpdatedAt(System.currentTimeMillis());
        
        String key = PROFILE_PREFIX + userId.toString();
        valkeyTemplate.opsForValue().set(key, profile, PROFILE_TTL);
        log.info("Updated readiness score for user: {} to {} for {}", userId, score, context);
        
        return profile;
    }

    public void deleteProfile(UUID userId) {
        String key = PROFILE_PREFIX + userId.toString();
        valkeyTemplate.delete(key);
        log.info("Deleted profile for user: {}", userId);
    }
}