package com.coach.profile;

import com.coach.common.LearningContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProfileServiceTest {

    @Mock
    private RedisTemplate<String, Object> valkeyTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    private ProfileService profileService;

    @BeforeEach
    void setUp() {
        when(valkeyTemplate.opsForValue()).thenReturn(valueOperations);
        profileService = new ProfileService(valkeyTemplate);
    }

    @Test
    void shouldCreateProfile() {
        UUID userId = UUID.randomUUID();
        LearningContext context = new LearningContext("Czech", "A1", "en");

        UserProfile profile = profileService.createProfile(userId, "test@example.com", context, PlanType.FREE);

        assertNotNull(profile);
        assertEquals(userId, profile.getUserId());
        assertEquals("test@example.com", profile.getEmail());
        assertEquals(PlanType.FREE, profile.getPlanType());
        assertFalse(profile.isDiagnosticCompleted());

        verify(valueOperations).set(eq("profile:" + userId.toString()), any(UserProfile.class), any(Duration.class));
    }

    @Test
    void shouldGetProfile() {
        UUID userId = UUID.randomUUID();
        LearningContext context = new LearningContext("Czech", "A1", "hi");
        UserProfile expectedProfile = UserProfile.builder()
                .userId(userId)
                .context(context)
                .planType(PlanType.PREMIUM)
                .build();

        when(valueOperations.get("profile:" + userId.toString())).thenReturn(expectedProfile);

        UserProfile result = profileService.getProfile(userId);

        assertNotNull(result);
        assertEquals(PlanType.PREMIUM, result.getPlanType());
    }

    @Test
    void shouldReturnNullWhenProfileNotFound() {
        UUID userId = UUID.randomUUID();
        when(valueOperations.get("profile:" + userId.toString())).thenReturn(null);

        UserProfile result = profileService.getProfile(userId);

        assertNull(result);
    }

    @Test
    void shouldUpdateProfile() {
        UUID userId = UUID.randomUUID();
        LearningContext existingContext = new LearningContext("Czech", "A1", "en");
        UserProfile existingProfile = UserProfile.builder()
                .userId(userId)
                .context(existingContext)
                .planType(PlanType.FREE)
                .build();

        when(valueOperations.get("profile:" + userId.toString())).thenReturn(existingProfile);

        ProfileUpdateRequest request = new ProfileUpdateRequest();
        request.setNativeLanguage("bn");

        UserProfile result = profileService.updateProfile(userId, request);

        assertNotNull(result);
        assertEquals("bn", result.getNativeLanguage());
    }

    @Test
    void shouldUpdateReadinessScore() {
        UUID userId = UUID.randomUUID();
        LearningContext existingContext = new LearningContext("Czech", "A1", "en");
        UserProfile existingProfile = UserProfile.builder()
                .userId(userId)
                .context(existingContext)
                .planType(PlanType.FREE)
                .readinessScore(0.0)
                .diagnosticCompleted(false)
                .build();

        when(valueOperations.get("profile:" + userId.toString())).thenReturn(existingProfile);

        LearningContext newContext = new LearningContext("Czech", "A2", "en");
        UserProfile result = profileService.updateReadinessScore(userId, 85.0, newContext);

        assertNotNull(result);
        assertEquals(85.0, result.getReadinessScore());
        assertEquals("A2", result.getTargetLevel());
        assertTrue(result.isDiagnosticCompleted());
    }

    @Test
    void shouldDeleteProfile() {
        UUID userId = UUID.randomUUID();

        profileService.deleteProfile(userId);

        verify(valkeyTemplate).delete("profile:" + userId.toString());
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentProfile() {
        UUID userId = UUID.randomUUID();
        when(valueOperations.get("profile:" + userId.toString())).thenReturn(null);

        ProfileUpdateRequest request = new ProfileUpdateRequest();
        request.setNativeLanguage("uk");

        assertThrows(RuntimeException.class, () -> profileService.updateProfile(userId, request));
    }
}