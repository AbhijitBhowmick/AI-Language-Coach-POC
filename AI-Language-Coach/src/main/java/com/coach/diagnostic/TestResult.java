package com.coach.diagnostic;

import java.util.List;

public class TestResult {
    private String targetLanguage;
    private String targetLevel;
    private int totalQuestions;
    private int correctAnswers;
    private double scorePercentage;
    private String recommendedLevel;
    private List<QuestionFeedback> feedback;

    public TestResult() {}

    public TestResult(String targetLanguage, String targetLevel, int totalQuestions, int correctAnswers, 
                    double scorePercentage, String recommendedLevel, List<QuestionFeedback> feedback) {
        this.targetLanguage = targetLanguage != null ? targetLanguage : "Czech";
        this.targetLevel = targetLevel != null ? targetLevel : "A1";
        this.totalQuestions = totalQuestions;
        this.correctAnswers = correctAnswers;
        this.scorePercentage = scorePercentage;
        this.recommendedLevel = recommendedLevel;
        this.feedback = feedback;
    }

    public static TestResultBuilder builder() {
        return new TestResultBuilder();
    }

    public String getTargetLanguage() { return targetLanguage != null ? targetLanguage : "Czech"; }
    public void setTargetLanguage(String targetLanguage) { this.targetLanguage = targetLanguage; }
    public String getTargetLevel() { return targetLevel != null ? targetLevel : "A1"; }
    public void setTargetLevel(String targetLevel) { this.targetLevel = targetLevel; }
    public int getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }
    public int getCorrectAnswers() { return correctAnswers; }
    public void setCorrectAnswers(int correctAnswers) { this.correctAnswers = correctAnswers; }
    public double getScorePercentage() { return scorePercentage; }
    public void setScorePercentage(double scorePercentage) { this.scorePercentage = scorePercentage; }
    public String getRecommendedLevel() { return recommendedLevel; }
    public void setRecommendedLevel(String recommendedLevel) { this.recommendedLevel = recommendedLevel; }
    public List<QuestionFeedback> getFeedback() { return feedback; }
    public void setFeedback(List<QuestionFeedback> feedback) { this.feedback = feedback; }

    public static class TestResultBuilder {
        private String targetLanguage;
        private String targetLevel;
        private int totalQuestions;
        private int correctAnswers;
        private double scorePercentage;
        private String recommendedLevel;
        private List<QuestionFeedback> feedback;

        public TestResultBuilder() {}

        public TestResultBuilder targetLanguage(String targetLanguage) { this.targetLanguage = targetLanguage; return this; }
        public TestResultBuilder targetLevel(String targetLevel) { this.targetLevel = targetLevel; return this; }
        public TestResultBuilder totalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; return this; }
        public TestResultBuilder correctAnswers(int correctAnswers) { this.correctAnswers = correctAnswers; return this; }
        public TestResultBuilder scorePercentage(double scorePercentage) { this.scorePercentage = scorePercentage; return this; }
        public TestResultBuilder recommendedLevel(String recommendedLevel) { this.recommendedLevel = recommendedLevel; return this; }
        public TestResultBuilder feedback(List<QuestionFeedback> feedback) { this.feedback = feedback; return this; }
        public TestResult build() {
            return new TestResult(
                targetLanguage, 
                targetLevel, 
                totalQuestions, 
                correctAnswers, 
                scorePercentage, 
                recommendedLevel, 
                feedback
            );
        }
    }

    public static class QuestionFeedback {
        private int questionNumber;
        private boolean correct;
        private String correctAnswer;
        private String explanation;

        public QuestionFeedback() {}

        public QuestionFeedback(int questionNumber, boolean correct, String correctAnswer, String explanation) {
            this.questionNumber = questionNumber;
            this.correct = correct;
            this.correctAnswer = correctAnswer;
            this.explanation = explanation;
        }

        public static QuestionFeedbackBuilder builder() {
            return new QuestionFeedbackBuilder();
        }

        public int getQuestionNumber() { return questionNumber; }
        public void setQuestionNumber(int questionNumber) { this.questionNumber = questionNumber; }
        public boolean isCorrect() { return correct; }
        public void setCorrect(boolean correct) { this.correct = correct; }
        public String getCorrectAnswer() { return correctAnswer; }
        public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }
        public String getExplanation() { return explanation; }
        public void setExplanation(String explanation) { this.explanation = explanation; }

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