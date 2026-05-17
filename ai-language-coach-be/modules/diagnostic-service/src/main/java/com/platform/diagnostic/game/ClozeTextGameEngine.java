package com.platform.diagnostic.game;

import com.platform.diagnostic.dto.*;

import java.util.*;

public class ClozeTextGameEngine implements GameEngine {

    private final GameTemplateFactory factory;

    public ClozeTextGameEngine(GameTemplateFactory factory) {
        this.factory = factory;
    }

    @Override
    public String getTemplateId() {
        return "cloze_text";
    }

    @Override
    public GameType getGameType() {
        return GameType.CLOZE_TEXT;
    }

    @Override
    public GameRenderData render(GameQuestion question, GameState state) {
        String text = question.questionText();
        List<GameQuestion.GapPosition> gaps = question.gapPositions();
        List<String> answerSlots = new ArrayList<>();

        if (gaps != null) {
            for (int i = 0; i < gaps.size(); i++) {
                answerSlots.add("gap_" + i);
            }
        }

        List<GameRenderData.OptionData> options = new ArrayList<>();
        if (question.correctAnswers() != null) {
            for (int i = 0; i < question.correctAnswers().size(); i++) {
                String ans = question.correctAnswers().get(i);
                options.add(new GameRenderData.OptionData("gap_opt_" + i, ans, null, true));
            }
            if (gaps != null && gaps.size() > question.correctAnswers().size()) {
                for (int i = question.correctAnswers().size(); i < gaps.size() * 2; i++) {
                    options.add(new GameRenderData.OptionData("gap_opt_" + i, "option_" + i, null, false));
                }
            }
            Collections.shuffle(options);
        }

        return GameRenderData.builder()
            .templateId(getTemplateId())
            .displayName("Fill in the Blank")
            .questionNumber(question.questionNumber())
            .totalQuestions(state.totalQuestions())
            .instructionText("Fill in the blanks with the correct word")
            .questionText(text)
            .answerSlots(answerSlots)
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
        List<String> filledGaps = answer.filledGaps();
        List<GameQuestion.GapPosition> gaps = question.gapPositions();
        List<String> correctAnswers = question.correctAnswers();

        if (filledGaps == null || gaps == null || correctAnswers == null) {
            return GameResult.builder().correct(false).pointsEarned(0).build();
        }

        int correctGaps = 0;
        for (int i = 0; i < Math.min(filledGaps.size(), gaps.size()); i++) {
            String userGap = filledGaps.get(i).toLowerCase().trim();
            GameQuestion.GapPosition gap = gaps.get(i);
            if (gap.acceptableAnswers() != null) {
                for (String acceptable : gap.acceptableAnswers()) {
                    if (userGap.equals(acceptable.toLowerCase().trim())) {
                        correctGaps++;
                        break;
                    }
                }
            } else if (i < correctAnswers.size() && userGap.equals(correctAnswers.get(i).toLowerCase().trim())) {
                correctGaps++;
            }
        }

        boolean allCorrect = correctGaps == gaps.size();
        int basePoints = question.pointsValue() > 0 ? question.pointsValue() : factory.getPointsPerCorrect(getTemplateId());
        int earnedPoints = allCorrect ? basePoints : (correctGaps * (basePoints / Math.max(1, gaps.size())));

        return GameResult.builder()
            .correct(allCorrect)
            .pointsEarned(earnedPoints)
            .correctAnswer(String.join(", ", correctAnswers))
            .explanation(question.explanation())
            .linguisticBridge(question.linguisticBridge())
            .responseTimeMs(answer.responseTimeMs())
            .timeout(answer.timeout())
            .build();
    }

    @Override
    public ValidationResult validateAnswer(GameAnswer answer) {
        if (answer.filledGaps() == null || answer.filledGaps().isEmpty()) {
            return ValidationResult.failure("No gaps filled");
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