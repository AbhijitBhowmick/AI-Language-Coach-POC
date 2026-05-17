package com.platform.diagnostic.game;

import com.platform.diagnostic.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class AnagramGameEngine implements GameEngine {

    private static final Logger log = LoggerFactory.getLogger(AnagramGameEngine.class);
    private final GameTemplateFactory factory;

    public AnagramGameEngine(GameTemplateFactory factory) {
        this.factory = factory;
    }

    @Override
    public String getTemplateId() {
        return "anagram";
    }

    @Override
    public GameType getGameType() {
        return GameType.ANAGRAM;
    }

    @Override
    public GameRenderData render(GameQuestion question, GameState state) {
        String word = question.correctAnswer();
        List<String> scrambled = new ArrayList<>();
        
        if (word != null) {
            char[] chars = word.toCharArray();
            List<Character> charList = new ArrayList<>();
            for (char c : chars) charList.add(c);
            Collections.shuffle(charList);
            for (char c : charList) scrambled.add(String.valueOf(c));
        }

        return GameRenderData.builder()
            .templateId(getTemplateId())
            .displayName("Anagram")
            .questionNumber(question.questionNumber())
            .totalQuestions(state.totalQuestions())
            .instructionText("Arrange letters to form the correct word")
            .questionText(question.questionText())
            .scrambledWords(scrambled)
            .timeLimitSeconds(question.timeLimitSeconds() > 0 ? question.timeLimitSeconds() : factory.getDefaultTimeSeconds(getTemplateId()))
            .pointsValue(question.pointsValue() > 0 ? question.pointsValue() : factory.getPointsPerCorrect(getTemplateId()))
            .livesRemaining(state.livesRemaining())
            .currentScore(state.score())
            .streakCount(state.streakCount())
            .build();
    }

    @Override
    public GameResult evaluate(GameAnswer answer, GameQuestion question) {
        String userAnswer = answer.answer();
        String correct = question.correctAnswer();
        
        boolean correctAnswer = false;
        if (userAnswer != null && correct != null) {
            String normalizedUser = userAnswer.replaceAll("\\s", "").toLowerCase();
            String normalizedCorrect = correct.replaceAll("\\s", "").toLowerCase();
            correctAnswer = normalizedUser.equals(normalizedCorrect);
        }

        int pointsEarned = 0;
        int penalty = factory.getPenaltyPoints(getTemplateId());
        
        if (correctAnswer) {
            pointsEarned = question.pointsValue() > 0 ? question.pointsValue() : factory.getPointsPerCorrect(getTemplateId());
            if (answer.responseTimeMs() < 10000) {
                pointsEarned += 5;
            }
        } else if (penalty != 0) {
            pointsEarned = penalty;
        }

        return GameResult.builder()
            .correct(correctAnswer)
            .pointsEarned(pointsEarned)
            .correctAnswer(correct)
            .explanation(question.explanation())
            .linguisticBridge(question.linguisticBridge())
            .responseTimeMs(answer.responseTimeMs())
            .timeout(answer.timeout())
            .build();
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
        return true;
    }

    @Override
    public boolean supportsBranching() {
        return false;
    }
}