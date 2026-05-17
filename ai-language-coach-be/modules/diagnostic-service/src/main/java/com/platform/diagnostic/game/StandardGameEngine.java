package com.platform.diagnostic.game;

import com.platform.diagnostic.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class StandardGameEngine implements GameEngine {

    private static final Logger log = LoggerFactory.getLogger(StandardGameEngine.class);
    private final GameTemplateFactory factory;

    public StandardGameEngine(GameTemplateFactory factory) {
        this.factory = factory;
    }

    @Override
    public String getTemplateId() {
        return "standard";
    }

    @Override
    public GameType getGameType() {
        return GameType.STANDARD;
    }

    @Override
    public GameRenderData render(GameQuestion question, GameState state) {
        List<GameRenderData.OptionData> options = new ArrayList<>();
        if (question.options() != null) {
            for (int i = 0; i < question.options().size(); i++) {
                String opt = question.options().get(i);
                boolean isCorrect = opt.equalsIgnoreCase(question.correctAnswer());
                options.add(new GameRenderData.OptionData("opt_" + i, opt, null, isCorrect));
            }
            Collections.shuffle(options);
        }

        String instruction = "Select the correct answer";
        if (question.skillArea() != null) {
            instruction = switch (question.skillArea().toLowerCase()) {
                case "listening" -> "Listen and select the correct answer";
                case "speaking" -> "Speak the correct answer";
                case "writing" -> "Type the correct answer";
                default -> "Select the correct answer";
            };
        }

        return GameRenderData.builder()
            .templateId(getTemplateId())
            .displayName("Quiz")
            .questionNumber(question.questionNumber())
            .totalQuestions(state.totalQuestions())
            .instructionText(instruction)
            .mediaUrl(question.mediaUrl())
            .mediaType(question.mediaType())
            .questionText(question.questionText())
            .options(options)
            .timeLimitSeconds(question.timeLimitSeconds() > 0 ? question.timeLimitSeconds() : factory.getDefaultTimeSeconds(getTemplateId()))
            .pointsValue(question.pointsValue() > 0 ? question.pointsValue() : factory.getPointsPerCorrect(getTemplateId()))
            .livesRemaining(state.livesRemaining())
            .currentScore(state.score())
            .streakCount(state.streakCount())
            .build();
    }

    @Override
    public GameResult evaluate(GameAnswer answer, GameQuestion question) {
        boolean correct = false;
        String submittedAnswer = answer.answer();
        
        if (submittedAnswer != null && question.correctAnswer() != null) {
            correct = submittedAnswer.equalsIgnoreCase(question.correctAnswer())
                || submittedAnswer.equalsIgnoreCase(question.correctAnswer().trim());
        }

        return buildResult(correct, answer, question);
    }

    @Override
    public ValidationResult validateAnswer(GameAnswer answer) {
        if (answer.answer() == null || answer.answer().isBlank()) {
            return ValidationResult.failure("Answer cannot be empty");
        }
        return ValidationResult.success();
    }

    @Override
    public int getDefaultTimeSeconds() {
        return factory.getDefaultTimeSeconds(getTemplateId());
    }

    @Override
    public boolean supportsLives() {
        return false;
    }

    @Override
    public boolean supportsBranching() {
        return false;
    }

    private GameResult buildResult(boolean correct, GameAnswer answer, GameQuestion question) {
        int pointsEarned = 0;
        int penalty = factory.getPenaltyPoints(getTemplateId());
        
        if (correct) {
            pointsEarned = question.pointsValue() > 0 ? question.pointsValue() : factory.getPointsPerCorrect(getTemplateId());
            if (answer.responseTimeMs() < 5000) {
                pointsEarned += 5;
            }
        } else if (penalty != 0) {
            pointsEarned = penalty;
        }

        return GameResult.builder()
            .correct(correct)
            .pointsEarned(pointsEarned)
            .correctAnswer(question.correctAnswer())
            .explanation(question.explanation())
            .linguisticBridge(question.linguisticBridge())
            .responseTimeMs(answer.responseTimeMs())
            .timeout(answer.timeout())
            .build();
    }
}