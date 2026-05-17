package com.platform.diagnostic.dto;

import java.util.List;
import java.util.Map;

public record GameAnswer(
    String sessionId,
    int questionIndex,
    String answer,
    List<String> selectedItems,
    Map<String, String> matchedPairs,
    String branchChoice,
    List<String> filledGaps,
    List<String> sortedCategories,
    long responseTimeMs,
    boolean timeout
) {
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String sessionId;
        private int questionIndex;
        private String answer;
        private List<String> selectedItems;
        private Map<String, String> matchedPairs;
        private String branchChoice;
        private List<String> filledGaps;
        private List<String> sortedCategories;
        private long responseTimeMs;
        private boolean timeout;

        public Builder sessionId(String v) { this.sessionId = v; return this; }
        public Builder questionIndex(int v) { this.questionIndex = v; return this; }
        public Builder answer(String v) { this.answer = v; return this; }
        public Builder selectedItems(List<String> v) { this.selectedItems = v; return this; }
        public Builder matchedPairs(Map<String, String> v) { this.matchedPairs = v; return this; }
        public Builder branchChoice(String v) { this.branchChoice = v; return this; }
        public Builder filledGaps(List<String> v) { this.filledGaps = v; return this; }
        public Builder sortedCategories(List<String> v) { this.sortedCategories = v; return this; }
        public Builder responseTimeMs(long v) { this.responseTimeMs = v; return this; }
        public Builder timeout(boolean v) { this.timeout = v; return this; }

        public GameAnswer build() {
            return new GameAnswer(sessionId, questionIndex, answer, selectedItems, matchedPairs,
                branchChoice, filledGaps, sortedCategories, responseTimeMs, timeout);
        }
    }
}