package com.platform.profile.service;

import com.platform.common.config.AppProperties;
import com.platform.profile.dto.CreateProfileRequest;
import com.platform.profile.dto.UpdateProfileRequest;
import com.platform.profile.dto.UserProfileResponse;
import com.platform.profile.entity.UserProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private RedisTemplate<String, Object> valkeyTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    private AppProperties appProperties;
    private ProfileService profileService;

    @BeforeEach
    void setUp() {
        appProperties = new AppProperties();
        appProperties.setTargetLanguage("Czech");
        appProperties.setTargetLevel("A1");
        appProperties.setNativeLanguage("en");
        lenient().when(valkeyTemplate.opsForValue()).thenReturn(valueOperations);
        profileService = new ProfileService(valkeyTemplate, appProperties);
    }

    @Test
    void createProfile_withDefaults_createsProfileWithDefaultValues() {
        UUID userId = UUID.randomUUID();
        String email = "test@test.com";
        CreateProfileRequest request = new CreateProfileRequest(null, null, null);

        UserProfileResponse response = profileService.createProfile(userId, email, request);

        assertEquals(userId, response.getUserId());
        assertEquals(email, response.getEmail());
        assertEquals("Czech", response.getTargetLanguage());
        assertEquals("A1", response.getTargetLevel());
        assertEquals("en", response.getNativeLanguage());
        assertEquals(0.0, response.getReadinessScore());
        assertFalse(response.getDiagnosticCompleted());
        assertNotNull(response.getCreatedAt());
        assertNotNull(response.getUpdatedAt());
        verify(valueOperations).set(startsWith("profile_v2:"), any(UserProfile.class), any());
    }

    @Test
    void createProfile_withCustomValues_createsProfileWithCustomValues() {
        UUID userId = UUID.randomUUID();
        CreateProfileRequest request = new CreateProfileRequest("German", "B1", "fr");

        UserProfileResponse response = profileService.createProfile(userId, "user@test.com", request);

        assertEquals("German", response.getTargetLanguage());
        assertEquals("B1", response.getTargetLevel());
        assertEquals("fr", response.getNativeLanguage());
    }

    @Test
    void getProfile_whenExists_returnsProfile() {
        UUID userId = UUID.randomUUID();
        UserProfile stored = UserProfile.builder()
                .userId(userId)
                .email("test@test.com")
                .targetLanguage("Czech")
                .targetLevel("A1")
                .nativeLanguage("en")
                .readinessScore(85.0)
                .diagnosticCompleted(true)
                .createdAt(1000L)
                .updatedAt(2000L)
                .build();
        when(valueOperations.get("profile_v2:" + userId)).thenReturn(stored);

        UserProfileResponse response = profileService.getProfile(userId);

        assertNotNull(response);
        assertEquals(userId, response.getUserId());
        assertEquals("Czech", response.getTargetLanguage());
        assertEquals("A1", response.getTargetLevel());
        assertEquals(85.0, response.getReadinessScore());
        assertTrue(response.getDiagnosticCompleted());
    }

    @Test
    void getProfile_whenNotExists_returnsNull() {
        UUID userId = UUID.randomUUID();
        when(valueOperations.get("profile_v2:" + userId)).thenReturn(null);

        UserProfileResponse response = profileService.getProfile(userId);

        assertNull(response);
    }

    @Test
    void updateProfile_updatesExistingProfile() {
        UUID userId = UUID.randomUUID();
        UserProfile existing = UserProfile.builder()
                .userId(userId)
                .email("test@test.com")
                .targetLanguage("Czech")
                .targetLevel("A1")
                .nativeLanguage("en")
                .readinessScore(50.0)
                .diagnosticCompleted(false)
                .createdAt(1000L)
                .updatedAt(1000L)
                .build();
        when(valueOperations.get("profile_v2:" + userId)).thenReturn(existing);

        UpdateProfileRequest update = new UpdateProfileRequest("German", "B1", null);
        UserProfileResponse updated = profileService.updateProfile(userId, update);

        assertEquals("German", updated.getTargetLanguage());
        assertEquals("B1", updated.getTargetLevel());
        assertEquals("en", updated.getNativeLanguage());
        assertEquals(50.0, updated.getReadinessScore());
        assertFalse(updated.getDiagnosticCompleted());
        verify(valueOperations, times(1)).set(anyString(), any(UserProfile.class), any());
    }

    @Test
    void deleteProfile_deletesFromRedis() {
        UUID userId = UUID.randomUUID();

        profileService.deleteProfile(userId);

        verify(valkeyTemplate).delete("profile_v2:" + userId);
    }

    @Test
    void createProfile_setsCorrectLearningContextValues() {
        UUID userId = UUID.randomUUID();
        CreateProfileRequest request = new CreateProfileRequest("German", "B2", "es");

        UserProfileResponse response = profileService.createProfile(userId, "u@t.com", request);

        assertEquals("German", response.getTargetLanguage());
        assertEquals("B2", response.getTargetLevel());
        assertEquals("es", response.getNativeLanguage());
    }
}