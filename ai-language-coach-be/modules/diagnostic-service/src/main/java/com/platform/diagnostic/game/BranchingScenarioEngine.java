package com.platform.diagnostic.game;

import com.platform.diagnostic.dto.*;

import java.util.*;

public class BranchingScenarioEngine implements GameEngine {

    private final GameTemplateFactory factory;

    public BranchingScenarioEngine(GameTemplateFactory factory) {
        this.factory = factory;
    }

    @Override
    public String getTemplateId() {
        return "situational_branching";
    }

    @Override
    public GameType getGameType() {
        return GameType.BRANCHING;
    }

    @Override
    public GameRenderData render(GameQuestion question, GameState state) {
        List<GameQuestion.BranchOption> branches = question.branchOptions();
        List<GameRenderData.BranchData> renderBranches = new ArrayList<>();

        if (branches != null) {
            for (GameQuestion.BranchOption branch : branches) {
                renderBranches.add(new GameRenderData.BranchData(
                    branch.id(),
                    branch.text(),
                    branch.nextQuestionId(),
                    branch.pointsReward(),
                    branch.isCorrect()
                ));
            }
        }

        return GameRenderData.builder()
            .templateId(getTemplateId())
            .displayName("Scenario")
            .questionNumber(question.questionNumber())
            .totalQuestions(state.totalQuestions())
            .instructionText("Choose your path - each choice matters!")
            .mediaUrl(question.mediaUrl())
            .mediaType(question.mediaType())
            .questionText(question.questionText())
            .branches(renderBranches)
            .timeLimitSeconds(question.timeLimitSeconds() > 0 ? question.timeLimitSeconds() : factory.getDefaultTimeSeconds(getTemplateId()))
            .pointsValue(question.pointsValue() > 0 ? question.pointsValue() : factory.getPointsPerCorrect(getTemplateId()))
            .livesRemaining(state.livesRemaining())
            .currentScore(state.score())
            .streakCount(state.streakCount())
            .build();
    }

    @Override
    public GameResult evaluate(GameAnswer answer, GameQuestion question) {
        String choice = answer.branchChoice();
        
        if (choice == null) {
            return GameResult.builder().correct(false).pointsEarned(0).build();
        }

        GameQuestion.BranchOption selectedBranch = null;
        if (question.branchOptions() != null) {
            for (GameQuestion.BranchOption branch : question.branchOptions()) {
                if (branch.id().equals(choice)) {
                    selectedBranch = branch;
                    break;
                }
            }
        }

        if (selectedBranch == null) {
            return GameResult.builder().correct(false).pointsEarned(0).build();
        }

        boolean correct = selectedBranch.isCorrect();
        int pointsEarned = correct ? selectedBranch.pointsReward() : 0;

        return GameResult.builder()
            .correct(correct)
            .pointsEarned(pointsEarned)
            .nextQuestionId(correct ? selectedBranch.nextQuestionId() : null)
            .explanation(correct ? "Great choice! You progressed." : "That path didn't lead to success. Try again!")
            .responseTimeMs(answer.responseTimeMs())
            .timeout(answer.timeout())
            .build();
    }

    @Override
    public ValidationResult validateAnswer(GameAnswer answer) {
        if (answer.branchChoice() == null || answer.branchChoice().isBlank()) {
            return ValidationResult.failure("Please select a choice");
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
        return true;
    }
}