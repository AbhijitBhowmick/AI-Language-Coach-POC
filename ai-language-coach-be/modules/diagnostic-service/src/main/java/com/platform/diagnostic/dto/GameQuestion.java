package com.platform.diagnostic.dto;

import java.util.List;
import java.util.Map;

public record GameQuestion(
    int questionNumber,
    String templateId,
    String targetLanguage,
    String targetLevel,
    String questionText,
    String mediaUrl,
    String mediaType,
    String situation,
    List<String> options,
    List<String> correctAnswers,
    List<GapPosition> gapPositions,
    List<MatchPair> matchPairs,
    List<BranchOption> branchOptions,
    List<CategoryItem> categoryItems,
    String correctAnswer,
    String explanation,
    String linguisticBridge,
    int timeLimitSeconds,
    int pointsValue,
    double difficultyWeight,
    String skillArea
) {
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private int questionNumber;
        private String templateId;
        private String targetLanguage = "Czech";
        private String targetLevel = "A1";
        private String questionText;
        private String mediaUrl;
        private String mediaType;
        private String situation;
        private List<String> options;
        private List<String> correctAnswers;
        private List<GapPosition> gapPositions;
        private List<MatchPair> matchPairs;
        private List<BranchOption> branchOptions;
        private List<CategoryItem> categoryItems;
        private String correctAnswer;
        private String explanation;
        private String linguisticBridge;
        private int timeLimitSeconds = 30;
        private int pointsValue = 10;
        private double difficultyWeight = 1.0;
        private String skillArea = "reading";

        public Builder questionNumber(int v) { this.questionNumber = v; return this; }
        public Builder templateId(String v) { this.templateId = v; return this; }
        public Builder targetLanguage(String v) { this.targetLanguage = v; return this; }
        public Builder targetLevel(String v) { this.targetLevel = v; return this; }
        public Builder questionText(String v) { this.questionText = v; return this; }
        public Builder mediaUrl(String v) { this.mediaUrl = v; return this; }
        public Builder mediaType(String v) { this.mediaType = v; return this; }
        public Builder situation(String v) { this.situation = v; return this; }
        public Builder options(List<String> v) { this.options = v; return this; }
        public Builder correctAnswers(List<String> v) { this.correctAnswers = v; return this; }
        public Builder gapPositions(List<GapPosition> v) { this.gapPositions = v; return this; }
        public Builder matchPairs(List<MatchPair> v) { this.matchPairs = v; return this; }
        public Builder branchOptions(List<BranchOption> v) { this.branchOptions = v; return this; }
        public Builder categoryItems(List<CategoryItem> v) { this.categoryItems = v; return this; }
        public Builder correctAnswer(String v) { this.correctAnswer = v; return this; }
        public Builder explanation(String v) { this.explanation = v; return this; }
        public Builder linguisticBridge(String v) { this.linguisticBridge = v; return this; }
        public Builder timeLimitSeconds(int v) { this.timeLimitSeconds = v; return this; }
        public Builder pointsValue(int v) { this.pointsValue = v; return this; }
        public Builder difficultyWeight(double v) { this.difficultyWeight = v; return this; }
        public Builder skillArea(String v) { this.skillArea = v; return this; }

        public GameQuestion build() {
            return new GameQuestion(questionNumber, templateId, targetLanguage, targetLevel,
                questionText, mediaUrl, mediaType, situation, options, correctAnswers,
                gapPositions, matchPairs, branchOptions, categoryItems, correctAnswer,
                explanation, linguisticBridge, timeLimitSeconds, pointsValue, difficultyWeight, skillArea);
        }
    }

    public record GapPosition(int position, int length, List<String> acceptableAnswers) {}
    public record MatchPair(String left, String right, String imageUrl) {}
    public record BranchOption(String id, String text, String nextQuestionId, int pointsReward, boolean isCorrect) {}
    public record CategoryItem(String id, String text, String category, String imageUrl) {}
}