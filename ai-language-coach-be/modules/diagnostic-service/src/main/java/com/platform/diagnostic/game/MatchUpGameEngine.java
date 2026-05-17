package com.platform.diagnostic.game;

import com.platform.diagnostic.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class MatchUpGameEngine implements GameEngine {

    private static final Logger log = LoggerFactory.getLogger(MatchUpGameEngine.class);
    private final GameTemplateFactory factory;
    private static final int PAIRS_TO_REVEAL = 2;

    public MatchUpGameEngine(GameTemplateFactory factory) {
        this.factory = factory;
    }

    @Override
    public String getTemplateId() {
        return "match_up";
    }

    @Override
    public GameType getGameType() {
        return GameType.MATCH_UP;
    }

    @Override
    public GameRenderData render(GameQuestion question, GameState state) {
        List<GameQuestion.MatchPair> matchPairs = question.matchPairs();
        List<GameRenderData.TargetItem> targetItems = new ArrayList<>();

        if (matchPairs != null) {
            for (int i = 0; i < matchPairs.size(); i++) {
                GameQuestion.MatchPair pair = matchPairs.get(i);
                targetItems.add(new GameRenderData.TargetItem("left_" + i, pair.left(), pair.imageUrl(), true));
                targetItems.add(new GameRenderData.TargetItem("right_" + i, pair.right(), null, false));
            }
            Collections.shuffle(targetItems);
        }

        return GameRenderData.builder()
            .templateId(getTemplateId())
            .displayName("Match Up")
            .questionNumber(question.questionNumber())
            .totalQuestions(state.totalQuestions())
            .instructionText("Tap pairs of matching items")
            .mediaUrl(question.mediaUrl())
            .mediaType(question.mediaType())
            .questionText(question.questionText())
            .targetItems(targetItems)
            .timeLimitSeconds(question.timeLimitSeconds() > 0 ? question.timeLimitSeconds() : factory.getDefaultTimeSeconds(getTemplateId()))
            .pointsValue(question.pointsValue() > 0 ? question.pointsValue() : factory.getPointsPerCorrect(getTemplateId()))
            .livesRemaining(state.livesRemaining())
            .currentScore(state.score())
            .streakCount(state.streakCount())
            .additionalData(Map.of("pairsToReveal", PAIRS_TO_REVEAL))
            .build();
    }

    @Override
    public GameResult evaluate(GameAnswer answer, GameQuestion question) {
        Map<String, String> matchedPairs = answer.matchedPairs();
        List<GameQuestion.MatchPair> correctPairs = question.matchPairs();
        
        if (matchedPairs == null || correctPairs == null) {
            return GameResult.builder().correct(false).pointsEarned(0).build();
        }

        int correctMatches = 0;
        for (GameQuestion.MatchPair pair : correctPairs) {
            String matchedValue = matchedPairs.get(pair.left());
            if (matchedValue != null && matchedValue.equals(pair.right())) {
                correctMatches++;
            }
        }

        boolean allCorrect = correctMatches == correctPairs.size();
        
        return GameResult.builder()
            .correct(allCorrect)
            .pointsEarned(allCorrect ? (correctMatches * (question.pointsValue() > 0 ? question.pointsValue() / 2 : 5)) : 0)
            .responseTimeMs(answer.responseTimeMs())
            .timeout(answer.timeout())
            .build();
    }

    @Override
    public ValidationResult validateAnswer(GameAnswer answer) {
        if (answer.matchedPairs() == null || answer.matchedPairs().isEmpty()) {
            return ValidationResult.failure("No matches selected");
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
}