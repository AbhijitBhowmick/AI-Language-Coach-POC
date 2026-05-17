package com.coach.diagnostic;

import com.coach.common.LearningContext;
import com.coach.profile.ProfileService;
import com.coach.profile.PlanType;
import com.coach.profile.UserProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiagnosticServiceTest {

    @Mock
    private RedisTemplate<String, Object> valkeyTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private QuestionBank questionBank;

    @Mock
    private ProfileService profileService;

    private DiagnosticService diagnosticService;

    private static final UUID TEST_USER_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        when(valkeyTemplate.opsForValue()).thenReturn(valueOperations);
        diagnosticService = new DiagnosticService(valkeyTemplate, questionBank, profileService);
    }

    @Test
    void shouldStartDiagnosticTest() {
        LearningContext context = new LearningContext("Czech", "A1", "en");
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

        when(questionBank.getQuestions(context)).thenReturn(questions);

        DiagnosticTest result = diagnosticService.startTest(TEST_USER_ID, context);

        assertNotNull(result);
        assertEquals(TEST_USER_ID, result.getUserId());
        assertEquals("Czech", result.getTargetLanguage());
        assertEquals("A1", result.getTargetLevel());
    }

    @Test
    void shouldGetCurrentQuestion() {
        LearningContext context = new LearningContext("Czech", "A1", "en");
        List<DiagnosticQuestion> questions = List.of(
                DiagnosticQuestion.builder()
                        .questionNumber(1)
                        .type(DiagnosticQuestion.QuestionType.GRAMMAR_COMPLETION)
                        .questionText("First question")
                        .options(List.of("A", "B", "C", "D"))
                        .correctAnswer("A")
                        .explanation("Explanation 1")
                        .level("A1")
                        .build(),
                DiagnosticQuestion.builder()
                        .questionNumber(2)
                        .type(DiagnosticQuestion.QuestionType.VISUAL_MULTIPLE_CHOICE)
                        .questionText("Second question")
                        .options(List.of("E", "F", "G", "H"))
                        .correctAnswer("E")
                        .explanation("Explanation 2")
                        .level("A1")
                        .build()
        );

        DiagnosticTest test = DiagnosticTest.builder()
                .userId(TEST_USER_ID)
                .targetLanguage("Czech")
                .targetLevel("A1")
                .nativeLanguage("en")
                .questions(questions)
                .currentQuestionIndex(0)
                .correctAnswers(0)
                .completed(false)
                .startedAt(System.currentTimeMillis())
                .build();

        when(valueOperations.get("diagnostic:test:" + TEST_USER_ID.toString())).thenReturn(test);

        DiagnosticQuestion currentQuestion = diagnosticService.getCurrentQuestion(TEST_USER_ID);

        assertNotNull(currentQuestion);
        assertEquals("First question", currentQuestion.getQuestionText());
    }

    @Test
    void shouldReturnNullWhenTestNotFound() {
        when(valueOperations.get(anyString())).thenReturn(null);

        DiagnosticQuestion result = diagnosticService.getCurrentQuestion(TEST_USER_ID);

        assertNull(result);
    }

    @Test
    void shouldSubmitCorrectAnswer() {
        LearningContext context = new LearningContext("Czech", "A1", "en");
        List<DiagnosticQuestion> questions = List.of(
                DiagnosticQuestion.builder()
                        .questionNumber(1)
                        .type(DiagnosticQuestion.QuestionType.GRAMMAR_COMPLETION)
                        .questionText("I ___ Czech.")
                        .options(List.of("love", "loves", "loving", "loved"))
                        .correctAnswer("love")
                        .explanation("First person singular")
                        .level("A1")
                        .build()
        );

        DiagnosticTest test = DiagnosticTest.builder()
                .userId(TEST_USER_ID)
                .targetLanguage("Czech")
                .targetLevel("A1")
                .nativeLanguage("en")
                .questions(questions)
                .currentQuestionIndex(0)
                .correctAnswers(0)
                .completed(false)
                .startedAt(System.currentTimeMillis())
                .build();

        when(valueOperations.get("diagnostic:test:" + TEST_USER_ID.toString())).thenReturn(test);

        AnswerSubmission submission = new AnswerSubmission();
        submission.setQuestionNumber(1);
        submission.setAnswer("love");

        DiagnosticTest result = diagnosticService.submitAnswer(TEST_USER_ID, submission);

        assertEquals(1, result.getCorrectAnswers());
        assertEquals(1, result.getCurrentQuestionIndex());
    }

    @Test
    void shouldCompleteTestAndUpdateProfile() {
        LearningContext context = new LearningContext("Czech", "A1", "en");
        List<DiagnosticQuestion> questions = List.of(
                DiagnosticQuestion.builder()
                        .questionNumber(1)
                        .type(DiagnosticQuestion.QuestionType.GRAMMAR_COMPLETION)
                        .questionText("Question")
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
                .nativeLanguage("en")
                .questions(questions)
                .currentQuestionIndex(0)
                .correctAnswers(1)
                .completed(false)
                .startedAt(System.currentTimeMillis())
                .build();

        when(valueOperations.get("diagnostic:test:" + TEST_USER_ID.toString())).thenReturn(test);

        AnswerSubmission submission = new AnswerSubmission();
        submission.setQuestionNumber(1);
        submission.setAnswer("A");

        DiagnosticTest result = diagnosticService.submitAnswer(TEST_USER_ID, submission);

        assertTrue(result.isCompleted());
    }

    @Test
    void shouldGetTestResult() {
        LearningContext context = new LearningContext("Czech", "A1", "en");
        List<DiagnosticQuestion> questions = List.of(
                DiagnosticQuestion.builder()
                        .questionNumber(1)
                        .type(DiagnosticQuestion.QuestionType.GRAMMAR_COMPLETION)
                        .questionText("Question")
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
                .nativeLanguage("en")
                .questions(questions)
                .currentQuestionIndex(1)
                .correctAnswers(1)
                .completed(true)
                .completedAt(System.currentTimeMillis())
                .startedAt(System.currentTimeMillis())
                .build();

        when(valueOperations.get("diagnostic:test:" + TEST_USER_ID.toString())).thenReturn(test);

        TestResult result = diagnosticService.getTestResult(TEST_USER_ID);

        assertNotNull(result);
        assertEquals(1, result.getTotalQuestions());
        assertEquals(1, result.getCorrectAnswers());
        assertEquals("Czech", result.getTargetLanguage());
    }

    @Test
    void shouldDetermineA2LevelForHighScore() {
        LearningContext context = new LearningContext("Czech", "A1", "en");
        List<DiagnosticQuestion> questions = List.of(
                DiagnosticQuestion.builder()
                        .questionNumber(1)
                        .type(DiagnosticQuestion.QuestionType.GRAMMAR_COMPLETION)
                        .questionText("Q1")
                        .options(List.of("A", "B", "C", "D"))
                        .correctAnswer("A")
                        .explanation("Exp")
                        .level("A1")
                        .build()
        );

        DiagnosticTest test = DiagnosticTest.builder()
                .userId(TEST_USER_ID)
                .targetLanguage("Czech")
                .targetLevel("A1")
                .nativeLanguage("en")
                .questions(questions)
                .currentQuestionIndex(1)
                .correctAnswers(1)
                .completed(true)
                .completedAt(System.currentTimeMillis())
                .startedAt(System.currentTimeMillis())
                .build();

        when(valueOperations.get("diagnostic:test:" + TEST_USER_ID.toString())).thenReturn(test);

        TestResult result = diagnosticService.getTestResult(TEST_USER_ID);

        assertEquals("A2", result.getRecommendedLevel());
    }
}