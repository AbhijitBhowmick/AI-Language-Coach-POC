package com.platform.diagnostic.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TestResult(
        String targetLanguage,
        String targetLevel,
        int totalQuestions,
        int correctAnswers,
        double scorePercentage,
        String recommendedLevel,
        List<QuestionFeedback> feedback
) {
    public TestResult {
        if (targetLanguage == null) targetLanguage = "Czech";
        if (targetLevel == null) targetLevel = "A1";
    }

    public String getTargetLanguage() { return targetLanguage != null ? targetLanguage : "Czech"; }
    public String getTargetLevel() { return targetLevel != null ? targetLevel : "A1"; }

    public static TestResultBuilder builder() {
        return new TestResultBuilder();
    }

    public static class TestResultBuilder {
        private String targetLanguage;
        private String targetLevel;
        private int totalQuestions;
        private int correctAnswers;
        private double scorePercentage;
        private String recommendedLevel;
        private List<QuestionFeedback> feedback;

        public TestResultBuilder targetLanguage(String targetLanguage) { this.targetLanguage = targetLanguage; return this; }
        public TestResultBuilder targetLevel(String targetLevel) { this.targetLevel = targetLevel; return this; }
        public TestResultBuilder totalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; return this; }
        public TestResultBuilder correctAnswers(int correctAnswers) { this.correctAnswers = correctAnswers; return this; }
        public TestResultBuilder scorePercentage(double scorePercentage) { this.scorePercentage = scorePercentage; return this; }
        public TestResultBuilder recommendedLevel(String recommendedLevel) { this.recommendedLevel = recommendedLevel; return this; }
        public TestResultBuilder feedback(List<QuestionFeedback> feedback) { this.feedback = feedback; return this; }
        public TestResult build() {
            return new TestResult(targetLanguage, targetLevel, totalQuestions, correctAnswers, scorePercentage, recommendedLevel, feedback);
        }
    }

    public record QuestionFeedback(
            int questionNumber,
            boolean correct,
            String correctAnswer,
            String explanation
    ) {
        public static QuestionFeedbackBuilder builder() {
            return new QuestionFeedbackBuilder();
        }

        public static class QuestionFeedbackBuilder {
            private int questionNumber;
            private boolean correct;
            private String correctAnswer;
            private String explanation;

            public QuestionFeedbackBuilder questionNumber(int questionNumber) { this.questionNumber = questionNumber; return this; }
            public QuestionFeedbackBuilder correct(boolean correct) { this.correct = correct; return this; }
            public QuestionFeedbackBuilder correctAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; return this; }
            public QuestionFeedbackBuilder explanation(String explanation) { this.explanation = explanation; return this; }
            public QuestionFeedback build() {
                return new QuestionFeedback(questionNumber, correct, correctAnswer, explanation);
            }
        }
    }
}