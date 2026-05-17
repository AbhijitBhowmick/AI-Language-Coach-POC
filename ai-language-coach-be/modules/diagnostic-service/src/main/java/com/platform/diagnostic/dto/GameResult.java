package com.platform.diagnostic.dto;

public record GameResult(
    boolean correct,
    int pointsEarned,
    int totalScore,
    int streakCount,
    int livesRemaining,
    String correctAnswer,
    String explanation,
    String linguisticBridge,
    long responseTimeMs,
    boolean timeout,
    String nextQuestionId,
    boolean isCompleted,
    GameEndData endData
) {
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private boolean correct;
        private int pointsEarned;
        private int totalScore;
        private int streakCount;
        private int livesRemaining;
        private String correctAnswer;
        private String explanation;
        private String linguisticBridge;
        private long responseTimeMs;
        private boolean timeout;
        private String nextQuestionId;
        private boolean isCompleted;
        private GameEndData endData;

        public Builder correct(boolean v) { this.correct = v; return this; }
        public Builder pointsEarned(int v) { this.pointsEarned = v; return this; }
        public Builder totalScore(int v) { this.totalScore = v; return this; }
        public Builder streakCount(int v) { this.streakCount = v; return this; }
        public Builder livesRemaining(int v) { this.livesRemaining = v; return this; }
        public Builder correctAnswer(String v) { this.correctAnswer = v; return this; }
        public Builder explanation(String v) { this.explanation = v; return this; }
        public Builder linguisticBridge(String v) { this.linguisticBridge = v; return this; }
        public Builder responseTimeMs(long v) { this.responseTimeMs = v; return this; }
        public Builder timeout(boolean v) { this.timeout = v; return this; }
        public Builder nextQuestionId(String v) { this.nextQuestionId = v; return this; }
        public Builder isCompleted(boolean v) { this.isCompleted = v; return this; }
        public Builder endData(GameEndData v) { this.endData = v; return this; }

        public GameResult build() {
            return new GameResult(correct, pointsEarned, totalScore, streakCount, livesRemaining,
                correctAnswer, explanation, linguisticBridge, responseTimeMs, timeout,
                nextQuestionId, isCompleted, endData);
        }
    }

    public record GameEndData(
        int totalQuestions,
        int correctAnswers,
        int totalScore,
        int bestStreak,
        long totalTimeMs,
        String performanceRating,
        String recommendedLevel
    ) {}
}