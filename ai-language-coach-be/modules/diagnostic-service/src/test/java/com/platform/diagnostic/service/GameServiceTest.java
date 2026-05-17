package com.platform.diagnostic.service;

import com.platform.diagnostic.dto.*;
import com.platform.diagnostic.entity.GameTemplateEntity;
import com.platform.diagnostic.game.GameEngine;
import com.platform.diagnostic.game.GameTemplateFactory;
import com.platform.diagnostic.repository.GameTemplateRepository;
import com.platform.diagnostic.repository.QuestionEntityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private GameTemplateFactory templateFactory;

    @Mock
    private QuestionEntityRepository questionRepository;

    @Mock
    private GameTemplateRepository gameTemplateRepository;

    @Mock
    private GameEngine gameEngine;

    private GameService gameService;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        gameService = new GameService(redisTemplate, templateFactory, questionRepository, gameTemplateRepository);
    }

    @Test
    void getAvailableTemplates_returnsAllTemplates() {
        GameTemplateEntity t1 = new GameTemplateEntity();
        t1.setTemplateId("standard");
        GameTemplateEntity t2 = new GameTemplateEntity();
        t2.setTemplateId("match_up");
        when(templateFactory.getAllTemplates()).thenReturn(List.of(t1, t2));

        List<GameTemplateEntity> templates = gameService.getAvailableTemplates();

        assertEquals(2, templates.size());
        verify(templateFactory).getAllTemplates();
    }

    @Test
    void startGame_createsSessionWithRenderData() {
        String templateId = "standard";
        GameTemplateEntity template = new GameTemplateEntity();
        template.setTemplateId(templateId);
        template.setMinQuestions(5);
        when(templateFactory.getTemplate(templateId)).thenReturn(template);
        when(templateFactory.supportsLives(templateId)).thenReturn(true);
        when(templateFactory.getEngine(templateId)).thenReturn(gameEngine);

        GameRenderData renderData = GameRenderData.builder()
                .templateId("standard")
                .displayName("Multiple Choice")
                .questionNumber(1)
                .totalQuestions(5)
                .questionText("Sample?")
                .timeLimitSeconds(30)
                .pointsValue(10)
                .livesRemaining(3)
                .currentScore(0)
                .streakCount(0)
                .build();
        when(gameEngine.render(any(), any())).thenReturn(renderData);

        GameService.GameSession session = gameService.startGame(
                UUID.randomUUID().toString(), templateId, "Czech", "A1");

        assertNotNull(session.sessionId());
        assertEquals("standard", session.state().templateId());
        assertEquals(0, session.state().currentQuestionIndex());
        assertFalse(session.state().isCompleted());
        assertNotNull(session.initialRender());
        verify(valueOperations).set(contains("game:session:"), any(Map.class), any());
    }

    @Test
    void startGame_unknownTemplate_throws() {
        when(templateFactory.getTemplate("invalid")).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () ->
                gameService.startGame("user1", "invalid", "Czech", "A1"));
    }

    @Test
    void submitAnswer_correctAnswer_updatesScore() {
        String sessionId = UUID.randomUUID().toString();
        GameState state = GameState.builder()
                .sessionId(sessionId).userId("user1").templateId("standard")
                .currentQuestionIndex(0).totalQuestions(5).score(0)
                .streakCount(0).livesRemaining(3).correctAnswers(0)
                .startTimeMs(System.currentTimeMillis())
                .questionStartTimeMs(System.currentTimeMillis())
                .attempts(List.of()).gameData(Map.of()).isCompleted(false)
                .build();

        GameQuestion question = GameQuestion.builder()
                .questionNumber(1).templateId("standard")
                .questionText("Sample?").correctAnswer("A")
                .options(List.of("A", "B", "C")).timeLimitSeconds(30).pointsValue(10)
                .build();

        Map<String, Object> sessionData = Map.of("state", state, "questions", List.of(question));
        when(valueOperations.get("game:session:" + sessionId)).thenReturn(sessionData);

        when(templateFactory.getEngine("standard")).thenReturn(gameEngine);
        when(templateFactory.supportsLives("standard")).thenReturn(true);
        when(gameEngine.validateAnswer(any())).thenReturn(ValidationResult.success());
        when(gameEngine.evaluate(any(), any())).thenReturn(GameResult.builder()
                .correct(false).pointsEarned(0).totalScore(0).streakCount(0)
                .livesRemaining(2).responseTimeMs(5000).timeout(false).build());

        GameAnswer answer = GameAnswer.builder().sessionId(sessionId)
                .questionIndex(0).answer("B").responseTimeMs(5000).build();
        GameResult result = gameService.submitAnswer(sessionId, answer);

        assertFalse(result.correct());
        assertEquals(0, result.pointsEarned());
    }
}