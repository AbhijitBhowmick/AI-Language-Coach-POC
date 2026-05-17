package com.platform.diagnostic.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.platform.diagnostic.dto.*;
import com.platform.diagnostic.entity.GameTemplateEntity;
import com.platform.diagnostic.game.GameEngine;
import com.platform.diagnostic.game.GameTemplateFactory;
import com.platform.diagnostic.game.GameType;
import com.platform.diagnostic.repository.GameTemplateRepository;
import com.platform.diagnostic.repository.QuestionEntityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GameService {

    private static final Logger log = LoggerFactory.getLogger(GameService.class);
    private static final String GAME_SESSION_PREFIX = "game:session:";
    private static final Duration SESSION_TTL = Duration.ofMinutes(30);

    private final RedisTemplate<String, Object> redisTemplate;
    private final GameTemplateFactory templateFactory;
    private final QuestionEntityRepository questionRepository;
    private final GameTemplateRepository gameTemplateRepository;

    public GameService(RedisTemplate<String, Object> redisTemplate,
                      GameTemplateFactory templateFactory,
                      QuestionEntityRepository questionRepository,
                      GameTemplateRepository gameTemplateRepository) {
        this.redisTemplate = redisTemplate;
        this.templateFactory = templateFactory;
        this.questionRepository = questionRepository;
        this.gameTemplateRepository = gameTemplateRepository;
    }

    public GameSession startGame(String userId, String templateId, String targetLanguage, String targetLevel) {
        String sessionId = UUID.randomUUID().toString();
        
        GameTemplateEntity template = templateFactory.getTemplate(templateId);
        if (template == null) {
            throw new IllegalArgumentException("Unknown template: " + templateId);
        }

        List<GameQuestion> questions = loadQuestions(templateId, targetLanguage, targetLevel, template.getMinQuestions());
        
        GameState state = GameState.builder()
            .sessionId(sessionId)
            .userId(userId)
            .templateId(templateId)
            .currentQuestionIndex(0)
            .totalQuestions(questions.size())
            .score(0)
            .streakCount(0)
            .livesRemaining(templateFactory.supportsLives(templateId) ? 3 : 0)
            .correctAnswers(0)
            .startTimeMs(System.currentTimeMillis())
            .questionStartTimeMs(System.currentTimeMillis())
            .attempts(new ArrayList<>())
            .gameData(new HashMap<>())
            .isCompleted(false)
            .build();

        saveSession(sessionId, state, questions);
        
        GameEngine engine = templateFactory.getEngine(templateId);
        GameRenderData renderData = engine.render(questions.get(0), state);
        
        log.info("Started game session {} for user {} with template {}", sessionId, userId, templateId);
        
        return new GameSession(sessionId, state, questions, renderData);
    }

    public GameRenderData getCurrentQuestion(String sessionId) {
        var session = loadSession(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("Session not found: " + sessionId);
        }

        GameState state = session.state();
        if (state.isCompleted() || state.currentQuestionIndex() >= session.questions().size()) {
            return null;
        }

        GameQuestion question = session.questions().get(state.currentQuestionIndex());
        GameEngine engine = templateFactory.getEngine(state.templateId());
        
        return engine.render(question, state);
    }

    public GameResult submitAnswer(String sessionId, GameAnswer answer) {
        var session = loadSession(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("Session not found: " + sessionId);
        }

        GameState state = session.state();
        if (state.isCompleted()) {
            throw new IllegalStateException("Game already completed");
        }

        List<GameQuestion> questions = session.questions();
        int questionIndex = state.currentQuestionIndex();
        
        if (questionIndex >= questions.size()) {
            throw new IllegalStateException("No more questions");
        }

        GameQuestion question = questions.get(questionIndex);
        GameEngine engine = templateFactory.getEngine(state.templateId());

        ValidationResult validation = engine.validateAnswer(answer);
        if (!validation.valid()) {
            throw new IllegalArgumentException(String.join(", ", validation.errors()));
        }

        GameResult result = engine.evaluate(answer, question);

        int newScore = state.score() + result.pointsEarned();
        int newStreak = result.correct() ? state.streakCount() + 1 : 0;
        int newLives = state.livesRemaining();
        int newCorrectAnswers = state.correctAnswers() + (result.correct() ? 1 : 0);

        if (result.correct() == false && templateFactory.supportsLives(state.templateId())) {
            newLives = Math.max(0, state.livesRemaining() - 1);
        }

        List<com.platform.diagnostic.dto.GameState.QuestionAttempt> newAttempts = new ArrayList<>(state.attempts());
        newAttempts.add(new com.platform.diagnostic.dto.GameState.QuestionAttempt(
            questionIndex, result.correct(), result.pointsEarned(), answer.responseTimeMs()
        ));

        boolean isCompleted = newLives <= 0 || questionIndex + 1 >= questions.size();
        
        GameResult finalResult;
        if (isCompleted) {
            int totalTimeMs = (int) (System.currentTimeMillis() - state.startTimeMs());
            GameResult.GameEndData endData = buildEndData(state, newCorrectAnswers, newScore, newStreak, totalTimeMs);
            finalResult = GameResult.builder()
                .correct(result.correct())
                .pointsEarned(result.pointsEarned())
                .totalScore(newScore)
                .streakCount(newStreak)
                .livesRemaining(newLives)
                .isCompleted(true)
                .endData(endData)
                .build();
        } else {
            finalResult = GameResult.builder()
                .correct(result.correct())
                .pointsEarned(result.pointsEarned())
                .totalScore(newScore)
                .streakCount(newStreak)
                .livesRemaining(newLives)
                .explanation(result.explanation())
                .linguisticBridge(result.linguisticBridge())
                .responseTimeMs(result.responseTimeMs())
                .timeout(result.timeout())
                .build();
        }

        GameState updatedState = GameState.builder()
            .sessionId(sessionId)
            .userId(state.userId())
            .templateId(state.templateId())
            .currentQuestionIndex(questionIndex + 1)
            .totalQuestions(state.totalQuestions())
            .score(newScore)
            .streakCount(newStreak)
            .livesRemaining(newLives)
            .correctAnswers(newCorrectAnswers)
            .startTimeMs(state.startTimeMs())
            .questionStartTimeMs(System.currentTimeMillis())
            .attempts(newAttempts)
            .gameData(state.gameData())
            .isCompleted(isCompleted)
            .build();

        saveSession(sessionId, updatedState, questions);
        
        log.info("Answer submitted for session {}, correct: {}, new score: {}", sessionId, result.correct(), newScore);
        
        return finalResult;
    }

    public List<GameTemplateEntity> getAvailableTemplates() {
        return templateFactory.getAllTemplates();
    }

    public List<GameTemplateEntity> getTemplatesByCategory(String category) {
        return templateFactory.getTemplatesByCategory(category);
    }

    @Transactional
    public GameTemplateEntity createTemplate(GameTemplateEntity template) {
        template.setActive(true);
        GameTemplateEntity saved = gameTemplateRepository.save(template);
        log.info("Created game template: {} - {}", saved.getTemplateId(), saved.getDisplayName());
        templateFactory.reloadTemplates();
        return saved;
    }

    private List<GameQuestion> loadQuestions(String templateId, String targetLanguage, String targetLevel, int count) {
        List<GameQuestion> questions = new ArrayList<>();

        var entities = questionRepository.findByTargetLanguageAndTargetLevelAndActiveTrue(
            targetLanguage != null ? targetLanguage : "Czech",
            targetLevel != null ? targetLevel : "A1"
        );

        if (entities.isEmpty()) {
            entities = questionRepository.findByTargetLanguageAndTargetLevelAndActiveTrue("Czech", "A1");
        }

        List<com.platform.diagnostic.entity.QuestionEntity> shuffled = new ArrayList<>(entities);
        Collections.shuffle(shuffled);

        for (int i = 0; i < Math.min(count, shuffled.size()); i++) {
            var entity = shuffled.get(i);
            questions.add(convertToGameQuestion(entity, templateId, i + 1));
        }

        while (questions.size() < count) {
            int idx = questions.size() + 1;
            questions.add(createDefaultQuestion(templateId, idx));
        }

        return questions;
    }

    private GameQuestion convertToGameQuestion(com.platform.diagnostic.entity.QuestionEntity entity, String templateId, int qNum) {
        GameQuestion.Builder builder = GameQuestion.builder()
            .questionNumber(qNum)
            .templateId(templateId)
            .targetLanguage(entity.getTargetLanguage())
            .targetLevel(entity.getTargetLevel())
            .questionText(entity.getQuestionText())
            .correctAnswer(entity.getCorrectAnswer())
            .explanation(entity.getExplanation())
            .linguisticBridge(entity.getLinguisticBridgeEn())
            .timeLimitSeconds(30)
            .pointsValue(10)
            .difficultyWeight(1.0)
            .skillArea("reading");

        if (entity.getOptions() != null && !entity.getOptions().isBlank()) {
            builder.options(List.of(entity.getOptions().split("\\|")));
        }

        return builder.build();
    }

    private GameQuestion createDefaultQuestion(String templateId, int qNum) {
        return GameQuestion.builder()
            .questionNumber(qNum)
            .templateId(templateId)
            .questionText("Sample question " + qNum)
            .options(List.of("Option A", "Option B", "Option C", "Option D"))
            .correctAnswer("Option A")
            .explanation("This is a sample explanation")
            .timeLimitSeconds(30)
            .pointsValue(10)
            .build();
    }

    private GameResult.GameEndData buildEndData(GameState state, int correctAnswers, int totalScore, int bestStreak, long totalTimeMs) {
        double accuracy = state.totalQuestions() > 0 ? (double) correctAnswers / state.totalQuestions() * 100 : 0;
        
        String rating;
        if (accuracy >= 90) rating = "Excellent";
        else if (accuracy >= 70) rating = "Great";
        else if (accuracy >= 50) rating = "Good";
        else if (accuracy >= 30) rating = "Keep Practicing";
        else rating = "Needs Work";

        String recommendedLevel;
        if (accuracy >= 80) recommendedLevel = "A2";
        else if (accuracy >= 50) recommendedLevel = "A1";
        else recommendedLevel = "A1-BEGINNER";

        return new GameResult.GameEndData(
            state.totalQuestions(),
            correctAnswers,
            totalScore,
            bestStreak,
            totalTimeMs,
            rating,
            recommendedLevel
        );
    }

    private void saveSession(String sessionId, GameState state, List<GameQuestion> questions) {
        String key = GAME_SESSION_PREFIX + sessionId;
        Map<String, Object> data = new HashMap<>();
        data.put("state", state);
        data.put("questions", questions);
        redisTemplate.opsForValue().set(key, data, SESSION_TTL);
    }

    private record SessionData(GameState state, List<GameQuestion> questions) {}

    private final ObjectMapper objectMapper = new ObjectMapper();

    private SessionData loadSession(String sessionId) {
        String key = GAME_SESSION_PREFIX + sessionId;
        Object data = redisTemplate.opsForValue().get(key);
        
        if (data instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) data;
            Object stateObj = map.get("state");
            Object questionsObj = map.get("questions");
            
            GameState state = null;
            if (stateObj instanceof GameState gs) {
                state = gs;
            } else if (stateObj instanceof Map) {
                try {
                    state = objectMapper.convertValue(stateObj, GameState.class);
                } catch (Exception e) {
                    log.error("Failed to deserialize GameState for session {}: {}", sessionId, e.getMessage());
                }
            }
            
            List<GameQuestion> questions = null;
            if (questionsObj instanceof List<?> list) {
                if (!list.isEmpty() && list.get(0) instanceof GameQuestion) {
                    @SuppressWarnings("unchecked")
                    List<GameQuestion> qs = (List<GameQuestion>) list;
                    questions = qs;
                } else {
                    try {
                        questions = objectMapper.convertValue(list, new com.fasterxml.jackson.core.type.TypeReference<List<GameQuestion>>() {});
                    } catch (Exception e) {
                        log.error("Failed to deserialize GameQuestions for session {}: {}", sessionId, e.getMessage());
                    }
                }
            }
            
            if (state != null && questions != null) {
                return new SessionData(state, questions);
            }
        }
        return null;
    }

    public record GameSession(String sessionId, GameState state, List<GameQuestion> questions, GameRenderData initialRender) {}
}