package com.platform.diagnostic.dto;

import java.util.List;
import java.util.Map;

public record GameState(
    String sessionId,
    String userId,
    String templateId,
    int currentQuestionIndex,
    int totalQuestions,
    int score,
    int streakCount,
    int livesRemaining,
    int correctAnswers,
    long startTimeMs,
    Long questionStartTimeMs,
    List<QuestionAttempt> attempts,
    Map<String, Object> gameData,
    boolean isCompleted
) {
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String sessionId;
        private String userId;
        private String templateId;
        private int currentQuestionIndex;
        private int totalQuestions;
        private int score;
        private int streakCount;
        private int livesRemaining = 3;
        private int correctAnswers;
        private long startTimeMs;
        private Long questionStartTimeMs;
        private List<QuestionAttempt> attempts;
        private Map<String, Object> gameData;
        private boolean isCompleted;

        public Builder sessionId(String v) { this.sessionId = v; return this; }
        public Builder userId(String v) { this.userId = v; return this; }
        public Builder templateId(String v) { this.templateId = v; return this; }
        public Builder currentQuestionIndex(int v) { this.currentQuestionIndex = v; return this; }
        public Builder totalQuestions(int v) { this.totalQuestions = v; return this; }
        public Builder score(int v) { this.score = v; return this; }
        public Builder streakCount(int v) { this.streakCount = v; return this; }
        public Builder livesRemaining(int v) { this.livesRemaining = v; return this; }
        public Builder correctAnswers(int v) { this.correctAnswers = v; return this; }
        public Builder startTimeMs(long v) { this.startTimeMs = v; return this; }
        public Builder questionStartTimeMs(Long v) { this.questionStartTimeMs = v; return this; }
        public Builder attempts(List<QuestionAttempt> v) { this.attempts = v; return this; }
        public Builder gameData(Map<String, Object> v) { this.gameData = v; return this; }
        public Builder isCompleted(boolean v) { this.isCompleted = v; return this; }

        public GameState build() {
            return new GameState(sessionId, userId, templateId, currentQuestionIndex, totalQuestions,
                score, streakCount, livesRemaining, correctAnswers, startTimeMs, questionStartTimeMs,
                attempts, gameData, isCompleted);
        }
    }

    public record QuestionAttempt(int questionIndex, boolean correct, int pointsEarned, long responseTimeMs) {}
}