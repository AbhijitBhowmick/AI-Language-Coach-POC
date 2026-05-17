package com.coach.profile;

import com.coach.common.LearningContext;
import com.coach.test.TestConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@EnableAutoConfiguration(exclude = OAuth2ClientAutoConfiguration.class)
@Import(TestConfig.class)
class ProfileControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RedisTemplate<String, Object> valkeyTemplate;

    @MockBean
    private com.coach.identity.UserRepository userRepository;

    @Autowired
    private ProfileService profileService;

    private static final UUID TEST_USER_ID = UUID.randomUUID();
    private static final String BASE_URL = "/api/v1/profile";

    @BeforeEach
    void setUp() {
        ValueOperations<String, Object> valueOps = mock(ValueOperations.class);
        when(valkeyTemplate.opsForValue()).thenReturn(valueOps);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void shouldCreateProfile() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .param("targetLanguage", "Czech")
                        .param("targetLevel", "A1")
                        .param("nativeLanguage", "en")
                        .param("planType", "FREE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.context.targetLanguage").value("Czech"))
                .andExpect(jsonPath("$.planType").value("FREE"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void shouldGetProfile() throws Exception {
        LearningContext context = new LearningContext("Czech", "A2", "hi");
        UserProfile profile = UserProfile.builder()
                .userId(TEST_USER_ID)
                .context(context)
                .planType(PlanType.PREMIUM)
                .readinessScore(75.0)
                .build();

        ValueOperations<String, Object> valueOps = mock(ValueOperations.class);
        when(valkeyTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("profile:" + TEST_USER_ID.toString())).thenReturn(profile);

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.context.targetLanguage").value("Czech"))
                .andExpect(jsonPath("$.planType").value("PREMIUM"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void shouldUpdateProfile() throws Exception {
        LearningContext existingContext = new LearningContext("Czech", "A1", "en");
        UserProfile existingProfile = UserProfile.builder()
                .userId(TEST_USER_ID)
                .context(existingContext)
                .planType(PlanType.FREE)
                .build();

        ValueOperations<String, Object> valueOps = mock(ValueOperations.class);
        when(valkeyTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(anyString())).thenReturn(existingProfile);

        ProfileUpdateRequest request = new ProfileUpdateRequest();
        request.setNativeLanguage("bn");

        mockMvc.perform(put(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn401WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isUnauthorized());
    }
}