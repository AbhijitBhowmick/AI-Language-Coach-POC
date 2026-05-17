package com.platform.diagnostic.game;

import com.platform.diagnostic.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class WhackAMoleGameEngine implements GameEngine {

    private static final Logger log = LoggerFactory.getLogger(WhackAMoleGameEngine.class);
    private final GameTemplateFactory factory;

    public WhackAMoleGameEngine(GameTemplateFactory factory) {
        this.factory = factory;
    }

    @Override
    public String getTemplateId() {
        return "whack_mole";
    }

    @Override
    public GameType getGameType() {
        return GameType.WHACK_A_MOLE;
    }

    @Override
    public GameRenderData render(GameQuestion question, GameState state) {
        List<GameRenderData.OptionData> options = new ArrayList<>();
        
        if (question.options() != null) {
            String correct = question.correctAnswer();
            for (int i = 0; i < question.options().size(); i++) {
                String opt = question.options().get(i);
                options.add(new GameRenderData.OptionData("mole_" + i, opt, null, opt.equalsIgnoreCase(correct)));
            }
            Collections.shuffle(options);
        }

        return GameRenderData.builder()
            .templateId(getTemplateId())
            .displayName("Whack-a-Mole")
            .questionNumber(question.questionNumber())
            .totalQuestions(state.totalQuestions())
            .instructionText("Tap the correct answer before it disappears!")
            .questionText(question.questionText())
            .options(options)
            .timeLimitSeconds(question.timeLimitSeconds() > 0 ? question.timeLimitSeconds() : factory.getDefaultTimeSeconds(getTemplateId()))
            .pointsValue(question.pointsValue() > 0 ? question.pointsValue() : factory.getPointsPerCorrect(getTemplateId()))
            .livesRemaining(state.livesRemaining())
            .currentScore(state.score())
            .streakCount(state.streakCount())
            .additionalData(Map.of("speedMultiplier", 1.5, "moleDisplayTime", 2000))
            .build();
    }

    @Override
    public GameResult evaluate(GameAnswer answer, GameQuestion question) {
        String selectedAnswer = answer.answer();
        String correctAnswer = question.correctAnswer();
        
        boolean correct = selectedAnswer != null && correctAnswer != null 
            && selectedAnswer.equalsIgnoreCase(correctAnswer);

        int pointsEarned = 0;
        int penalty = factory.getPenaltyPoints(getTemplateId());
        
        if (correct) {
            pointsEarned = question.pointsValue() > 0 ? question.pointsValue() : factory.getPointsPerCorrect(getTemplateId());
            if (answer.responseTimeMs() < 3000) {
                pointsEarned += 10;
            } else if (answer.responseTimeMs() < 5000) {
                pointsEarned += 5;
            }
        } else {
            if (penalty != 0) pointsEarned = penalty;
        }

        return GameResult.builder()
            .correct(correct)
            .pointsEarned(pointsEarned)
            .correctAnswer(correctAnswer)
            .explanation(question.explanation())
            .linguisticBridge(question.linguisticBridge())
            .responseTimeMs(answer.responseTimeMs())
            .timeout(answer.timeout())
            .build();
    }

    @Override
    public ValidationResult validateAnswer(GameAnswer answer) {
        if (answer.answer() == null || answer.answer().isBlank()) {
            return ValidationResult.failure("No answer selected");
        }
        return ValidationResult.success();
    }

    @Override
    public int getDefaultTimeSeconds() {
        return factory.getDefaultTimeSeconds(getTemplateId());
    }

    @Override
    public boolean supportsLives() {
        return true;
    }

    @Override
    public boolean supportsBranching() {
        return false;
    }
}