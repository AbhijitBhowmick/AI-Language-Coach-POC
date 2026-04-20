package com.coach.diagnostic;

import com.coach.common.LearningContext;
import com.coach.common.config.ConfigService;
import com.coach.profile.ProfileService;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
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
class DiagnosticControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RedisTemplate<String, Object> valkeyTemplate;

    @MockBean
    private QuestionBank questionBank;

    @MockBean
    private ProfileService profileService;

    private static final UUID TEST_USER_ID = UUID.randomUUID();
    private static final String BASE_URL = "/api/v1/diagnostic";

    @BeforeEach
    void setUp() {
        ValueOperations<String, Object> valueOps = mock(ValueOperations.class);
        when(valkeyTemplate.opsForValue()).thenReturn(valueOps);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void shouldStartDiagnosticTest() throws Exception {
        List<DiagnosticQuestion> questions = List.of(
                DiagnosticQuestion.builder()
                        .questionNumber(1)
                        .type(DiagnosticQuestion.QuestionType.GRAMMAR_COMPLETION)
                        .questionText("Test question")
                        .options(List.of("A", "B", "C", "D"))
                        .correctAnswer("A")
                        .explanation("Test explanation")
                        .level("A1")
                        .build()
        );

        when(questionBank.getQuestions(any(LearningContext.class))).thenReturn(questions);

        mockMvc.perform(post(BASE_URL + "/start")
                        .param("targetLanguage", "Czech")
                        .param("targetLevel", "A1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.questions").isArray());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void shouldGetCurrentQuestion() throws Exception {
        List<DiagnosticQuestion> questions = List.of(
                DiagnosticQuestion.builder()
                        .questionNumber(1)
                        .type(DiagnosticQuestion.QuestionType.GRAMMAR_COMPLETION)
                        .questionText("First question?")
                        .options(List.of("A", "B", "C", "D"))
                        .correctAnswer("A")
                        .explanation("Explanation")
                        .level("A1")
                        .build()
        );

        DiagnosticTest test = DiagnosticTest.builder()
                .userId(TEST_USER_ID)
                .targetLanguage("Czech")
                .targetLevel("A1")
                .questions(questions)
                .currentQuestionIndex(0)
                .correctAnswers(0)
                .completed(false)
                .startedAt(System.currentTimeMillis())
                .build();

        ValueOperations<String, Object> valueOps = mock(ValueOperations.class);
        when(valkeyTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("diagnostic:test:" + TEST_USER_ID.toString())).thenReturn(test);

        mockMvc.perform(get(BASE_URL + "/question"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.questionText").value("First question?"));
    }

    @Test
    void shouldReturn401WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get(BASE_URL + "/question"))
                .andExpect(status().isUnauthorized());
    }
}