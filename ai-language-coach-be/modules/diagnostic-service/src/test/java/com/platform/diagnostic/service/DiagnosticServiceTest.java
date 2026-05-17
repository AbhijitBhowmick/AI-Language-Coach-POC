package com.platform.diagnostic.service;

import com.platform.common.model.LearningContext;
import com.platform.diagnostic.dto.AnswerSubmission;
import com.platform.diagnostic.dto.DiagnosticQuestion;
import com.platform.diagnostic.dto.DiagnosticTest;
import com.platform.diagnostic.dto.TestResult;
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
    private QuestionBankService questionBankService;

    private DiagnosticService diagnosticService;

    @BeforeEach
    void setUp() {
        lenient().when(valkeyTemplate.opsForValue()).thenReturn(valueOperations);
        diagnosticService = new DiagnosticService(valkeyTemplate, questionBankService);
    }

    private DiagnosticQuestion createQuestion(int num, String correctAnswer) {
        return DiagnosticQuestion.builder()
                .questionNumber(num)
                .type(DiagnosticQuestion.QuestionType.GRAMMAR_COMPLETION)
                .targetLanguage("Czech")
                .targetLevel("A1")
                .questionText("Sample question " + num)
                .options(List.of("opt1", "opt2", "opt3"))
                .correctAnswer(correctAnswer)
                .explanation("Explanation for question " + num)
                .build();
    }

    @Test
    void startTest_createsTestWithQuestions() {
        UUID userId = UUID.randomUUID();
        LearningContext context = new LearningContext("Czech", "A1", "en");
        List<DiagnosticQuestion> questions = List.of(
                createQuestion(1, "opt1"),
                createQuestion(2, "opt2")
        );
        when(questionBankService.getQuestions(context)).thenReturn(questions);

        DiagnosticTest test = diagnosticService.startTest(userId, context);

        assertEquals(userId, test.getUserId());
        assertEquals("Czech", test.getTargetLanguage());
        assertEquals("A1", test.getTargetLevel());
        assertEquals(2, test.getQuestions().size());
        assertEquals(0, test.getCurrentQuestionIndex());
        assertEquals(0, test.getCorrectAnswers());
        assertFalse(test.isCompleted());
        assertTrue(test.getStartedAt() > 0);
        verify(valueOperations).set(contains("diagnostic:test:"), any(DiagnosticTest.class), any());
    }

    @Test
    void getCurrentQuestion_whenTestInProgress_returnsCurrentQuestion() {
        UUID userId = UUID.randomUUID();
        List<DiagnosticQuestion> questions = List.of(
                createQuestion(1, "opt1"),
                createQuestion(2, "opt2")
        );
        DiagnosticTest test = DiagnosticTest.builder()
                .userId(userId)
                .targetLanguage("Czech")
                .targetLevel("A1")
                .questions(questions)
                .currentQuestionIndex(0)
                .correctAnswers(0)
                .completed(false)
                .startedAt(System.currentTimeMillis())
                .build();
        when(valueOperations.get("diagnostic:test:" + userId)).thenReturn(test);

        DiagnosticQuestion question = diagnosticService.getCurrentQuestion(userId);

        assertNotNull(question);
        assertEquals(1, question.getQuestionNumber());
    }

    @Test
    void getCurrentQuestion_whenTestCompleted_returnsNull() {
        UUID userId = UUID.randomUUID();
        DiagnosticTest test = DiagnosticTest.builder()
                .userId(userId)
                .targetLanguage("Czech")
                .targetLevel("A1")
                .questions(List.of(createQuestion(1, "opt1")))
                .currentQuestionIndex(1)
                .correctAnswers(1)
                .completed(true)
                .startedAt(System.currentTimeMillis())
                .completedAt(System.currentTimeMillis())
                .build();
        when(valueOperations.get("diagnostic:test:" + userId)).thenReturn(test);

        DiagnosticQuestion question = diagnosticService.getCurrentQuestion(userId);

        assertNull(question);
    }

    @Test
    void submitAnswer_correctAnswer_incrementsScore() {
        UUID userId = UUID.randomUUID();
        List<DiagnosticQuestion> questions = List.of(
                createQuestion(1, "correctAnswer")
        );
        DiagnosticTest test = DiagnosticTest.builder()
                .userId(userId)
                .targetLanguage("Czech")
                .targetLevel("A1")
                .questions(questions)
                .currentQuestionIndex(0)
                .correctAnswers(0)
                .completed(false)
                .startedAt(System.currentTimeMillis())
                .build();
        when(valueOperations.get("diagnostic:test:" + userId)).thenReturn(test);

        AnswerSubmission submission = new AnswerSubmission(1, "correctAnswer");
        DiagnosticTest updated = diagnosticService.submitAnswer(userId, submission);

        assertEquals(1, updated.getCorrectAnswers());
        assertEquals(1, updated.getCurrentQuestionIndex());
        assertTrue(updated.isCompleted());
        assertTrue(updated.getCompletedAt() > 0);
    }

    @Test
    void submitAnswer_wrongAnswer_doesNotIncrementScore() {
        UUID userId = UUID.randomUUID();
        List<DiagnosticQuestion> questions = List.of(
                createQuestion(1, "correctAnswer"),
                createQuestion(2, "opt2")
        );
        DiagnosticTest test = DiagnosticTest.builder()
                .userId(userId)
                .targetLanguage("Czech")
                .targetLevel("A1")
                .questions(questions)
                .currentQuestionIndex(0)
                .correctAnswers(0)
                .completed(false)
                .startedAt(System.currentTimeMillis())
                .build();
        when(valueOperations.get("diagnostic:test:" + userId)).thenReturn(test);

        AnswerSubmission submission = new AnswerSubmission(1, "wrongAnswer");
        DiagnosticTest updated = diagnosticService.submitAnswer(userId, submission);

        assertEquals(0, updated.getCorrectAnswers());
        assertEquals(1, updated.getCurrentQuestionIndex());
        assertFalse(updated.isCompleted());
    }

    @Test
    void submitAnswer_whenNoActiveTest_throws() {
        UUID userId = UUID.randomUUID();
        when(valueOperations.get("diagnostic:test:" + userId)).thenReturn(null);

        assertThrows(RuntimeException.class, () ->
                diagnosticService.submitAnswer(userId, new AnswerSubmission(1, "x")));
    }

    @Test
    void getTestResult_whenTestExists_returnsCalculatedResult() {
        UUID userId = UUID.randomUUID();
        List<DiagnosticQuestion> questions = List.of(
                createQuestion(1, "correct"),
                createQuestion(2, "correct")
        );
        DiagnosticTest test = DiagnosticTest.builder()
                .userId(userId)
                .targetLanguage("Czech")
                .targetLevel("A1")
                .questions(questions)
                .currentQuestionIndex(2)
                .correctAnswers(2)
                .completed(true)
                .startedAt(System.currentTimeMillis())
                .completedAt(System.currentTimeMillis())
                .build();
        when(valueOperations.get("diagnostic:test:" + userId)).thenReturn(test);

        TestResult result = diagnosticService.getTestResult(userId);

        assertEquals(2, result.totalQuestions());
        assertEquals(2, result.correctAnswers());
        assertEquals(100.0, result.scorePercentage());
        assertEquals("A2", result.recommendedLevel());
        assertEquals(2, result.feedback().size());
    }

    @Test
    void getTestResult_whenNoTest_throws() {
        UUID userId = UUID.randomUUID();
        when(valueOperations.get("diagnostic:test:" + userId)).thenReturn(null);

        assertThrows(RuntimeException.class, () -> diagnosticService.getTestResult(userId));
    }

    @Test
    void scoreMapping_above80_recommendsA2() {
        UUID userId = UUID.randomUUID();
        List<DiagnosticQuestion> questions = List.of(
                createQuestion(1, "correct"),
                createQuestion(2, "wrong")
        );
        DiagnosticTest test = DiagnosticTest.builder()
                .userId(userId).targetLanguage("Czech").targetLevel("A1")
                .questions(questions).currentQuestionIndex(2).correctAnswers(2)
                .completed(true).startedAt(1L).completedAt(2L)
                .build();
        when(valueOperations.get("diagnostic:test:" + userId)).thenReturn(test);

        TestResult result = diagnosticService.getTestResult(userId);

        assertEquals("A2", result.recommendedLevel());
    }

    @Test
    void scoreMapping_below50_recommendsA1_BEGINNER() {
        UUID userId = UUID.randomUUID();
        List<DiagnosticQuestion> questions = List.of(
                createQuestion(1, "correct"),
                createQuestion(2, "correct"),
                createQuestion(3, "correct")
        );
        DiagnosticTest test = DiagnosticTest.builder()
                .userId(userId).targetLanguage("Czech").targetLevel("A1")
                .questions(questions).currentQuestionIndex(3).correctAnswers(1)
                .completed(true).startedAt(1L).completedAt(2L)
                .build();
        when(valueOperations.get("diagnostic:test:" + userId)).thenReturn(test);

        assertEquals("A1-BEGINNER", diagnosticService.getTestResult(userId).recommendedLevel());
    }
}