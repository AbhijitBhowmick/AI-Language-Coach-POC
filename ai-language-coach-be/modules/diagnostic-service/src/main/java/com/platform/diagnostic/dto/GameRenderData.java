package com.platform.diagnostic.dto;

import java.util.List;
import java.util.Map;

public record GameRenderData(
    String templateId,
    String displayName,
    int questionNumber,
    int totalQuestions,
    String instructionText,
    String mediaUrl,
    String mediaType,
    String questionText,
    List<OptionData> options,
    List<String> answerSlots,
    List<String> scrambledWords,
    List<TargetItem> targetItems,
    List<BranchData> branches,
    List<CategoryData> categories,
    List<SortItem> sortItems,
    int timeLimitSeconds,
    int pointsValue,
    int livesRemaining,
    int currentScore,
    int streakCount,
    Map<String, Object> additionalData
) {
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String templateId;
        private String displayName;
        private int questionNumber;
        private int totalQuestions;
        private String instructionText;
        private String mediaUrl;
        private String mediaType;
        private String questionText;
        private List<OptionData> options;
        private List<String> answerSlots;
        private List<String> scrambledWords;
        private List<TargetItem> targetItems;
        private List<BranchData> branches;
        private List<CategoryData> categories;
        private List<SortItem> sortItems;
        private int timeLimitSeconds = 30;
        private int pointsValue = 10;
        private int livesRemaining = 3;
        private int currentScore;
        private int streakCount;
        private Map<String, Object> additionalData;

        public Builder templateId(String v) { this.templateId = v; return this; }
        public Builder displayName(String v) { this.displayName = v; return this; }
        public Builder questionNumber(int v) { this.questionNumber = v; return this; }
        public Builder totalQuestions(int v) { this.totalQuestions = v; return this; }
        public Builder instructionText(String v) { this.instructionText = v; return this; }
        public Builder mediaUrl(String v) { this.mediaUrl = v; return this; }
        public Builder mediaType(String v) { this.mediaType = v; return this; }
        public Builder questionText(String v) { this.questionText = v; return this; }
        public Builder options(List<OptionData> v) { this.options = v; return this; }
        public Builder answerSlots(List<String> v) { this.answerSlots = v; return this; }
        public Builder scrambledWords(List<String> v) { this.scrambledWords = v; return this; }
        public Builder targetItems(List<TargetItem> v) { this.targetItems = v; return this; }
        public Builder branches(List<BranchData> v) { this.branches = v; return this; }
        public Builder categories(List<CategoryData> v) { this.categories = v; return this; }
        public Builder sortItems(List<SortItem> v) { this.sortItems = v; return this; }
        public Builder timeLimitSeconds(int v) { this.timeLimitSeconds = v; return this; }
        public Builder pointsValue(int v) { this.pointsValue = v; return this; }
        public Builder livesRemaining(int v) { this.livesRemaining = v; return this; }
        public Builder currentScore(int v) { this.currentScore = v; return this; }
        public Builder streakCount(int v) { this.streakCount = v; return this; }
        public Builder additionalData(Map<String, Object> v) { this.additionalData = v; return this; }

        public GameRenderData build() {
            return new GameRenderData(templateId, displayName, questionNumber, totalQuestions,
                instructionText, mediaUrl, mediaType, questionText, options, answerSlots,
                scrambledWords, targetItems, branches, categories, sortItems, timeLimitSeconds,
                pointsValue, livesRemaining, currentScore, streakCount, additionalData);
        }
    }

    public record OptionData(String id, String text, String imageUrl, boolean isCorrect) {}
    public record TargetItem(String id, String text, String imageUrl, boolean isTarget) {}
    public record BranchData(String id, String text, String nextQuestionId, int pointsReward, boolean leadsToSuccess) {}
    public record CategoryData(String id, String name, List<CategoryItem> items) {}
    public record CategoryItem(String id, String text, String imageUrl, String assignedCategory) {}
    public record SortItem(String id, String text, String correctCategory) {}
}