package com.platform.diagnostic.game;

import com.platform.diagnostic.dto.*;

import java.util.HashMap;
import java.util.Map;

public class SpeakingCardGameEngine implements GameEngine {

    private final GameTemplateFactory factory;

    public SpeakingCardGameEngine(GameTemplateFactory factory) {
        this.factory = factory;
    }

    @Override
    public String getTemplateId() {
        return "speaking_card";
    }

    @Override
    public GameType getGameType() {
        return GameType.SPEAKING_CARD;
    }

    @Override
    public GameRenderData render(GameQuestion question, GameState state) {
        return GameRenderData.builder()
            .templateId(getTemplateId())
            .displayName("Speaking Card")
            .questionNumber(question.questionNumber())
            .totalQuestions(state.totalQuestions())
            .instructionText("Record your response")
            .mediaUrl(question.mediaUrl())
            .mediaType("image")
            .questionText(question.questionText())
            .timeLimitSeconds(question.timeLimitSeconds() > 0 ? question.timeLimitSeconds() : factory.getDefaultTimeSeconds(getTemplateId()))
            .pointsValue(question.pointsValue() > 0 ? question.pointsValue() : factory.getPointsPerCorrect(getTemplateId()))
            .livesRemaining(state.livesRemaining())
            .currentScore(state.score())
            .streakCount(state.streakCount())
            .additionalData(Map.of("audioRequired", true, "maxRecordingSeconds", 60))
            .build();
    }

    @Override
    public GameResult evaluate(GameAnswer answer, GameQuestion question) {
        return GameResult.builder()
            .correct(true)
            .pointsEarned(question.pointsValue() > 0 ? question.pointsValue() : factory.getPointsPerCorrect(getTemplateId()))
            .explanation("Recording submitted for review")
            .responseTimeMs(answer.responseTimeMs())
            .build();
    }

    @Override
    public ValidationResult validateAnswer(GameAnswer answer) {
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
}