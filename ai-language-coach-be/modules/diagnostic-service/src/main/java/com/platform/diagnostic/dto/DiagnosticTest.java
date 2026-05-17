package com.platform.diagnostic.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DiagnosticTest implements Serializable {
    private UUID userId;
    private String targetLanguage;
    private String targetLevel;
    private String nativeLanguage;
    private List<DiagnosticQuestion> questions;
    private int currentQuestionIndex;
    private int correctAnswers;
    private boolean completed;
    private long startedAt;
    private long completedAt;

    public DiagnosticTest() {}

    public DiagnosticTest(UUID userId, String targetLanguage, String targetLevel, String nativeLanguage,
                           List<DiagnosticQuestion> questions, int currentQuestionIndex,
                           int correctAnswers, boolean completed, long startedAt, long completedAt) {
        this.userId = userId;
        this.targetLanguage = targetLanguage != null ? targetLanguage : "Czech";
        this.targetLevel = targetLevel != null ? targetLevel : "A1";
        this.nativeLanguage = nativeLanguage != null ? nativeLanguage : "en";
        this.questions = questions;
        this.currentQuestionIndex = currentQuestionIndex;
        this.correctAnswers = correctAnswers;
        this.completed = completed;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
    }

    public static DiagnosticTestBuilder builder() {
        return new DiagnosticTestBuilder();
    }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getTargetLanguage() { return targetLanguage != null ? targetLanguage : "Czech"; }
    public void setTargetLanguage(String targetLanguage) { this.targetLanguage = targetLanguage; }
    public String getTargetLevel() { return targetLevel != null ? targetLevel : "A1"; }
    public void setTargetLevel(String targetLevel) { this.targetLevel = targetLevel; }
    public String getNativeLanguage() { return nativeLanguage != null ? nativeLanguage : "en"; }
    public void setNativeLanguage(String nativeLanguage) { this.nativeLanguage = nativeLanguage; }
    public List<DiagnosticQuestion> getQuestions() { return questions; }
    public void setQuestions(List<DiagnosticQuestion> questions) { this.questions = questions; }
    public int getCurrentQuestionIndex() { return currentQuestionIndex; }
    public void setCurrentQuestionIndex(int currentQuestionIndex) { this.currentQuestionIndex = currentQuestionIndex; }
    public int getCorrectAnswers() { return correctAnswers; }
    public void setCorrectAnswers(int correctAnswers) { this.correctAnswers = correctAnswers; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    public long getStartedAt() { return startedAt; }
    public void setStartedAt(long startedAt) { this.startedAt = startedAt; }
    public long getCompletedAt() { return completedAt; }
    public void setCompletedAt(long completedAt) { this.completedAt = completedAt; }

    public static class DiagnosticTestBuilder {
        private UUID userId;
        private String targetLanguage;
        private String targetLevel;
        private String nativeLanguage;
        private List<DiagnosticQuestion> questions;
        private int currentQuestionIndex;
        private int correctAnswers;
        private boolean completed;
        private long startedAt;
        private long completedAt;

        public DiagnosticTestBuilder() {}

        public DiagnosticTestBuilder userId(UUID userId) { this.userId = userId; return this; }
        public DiagnosticTestBuilder targetLanguage(String targetLanguage) { this.targetLanguage = targetLanguage; return this; }
        public DiagnosticTestBuilder targetLevel(String targetLevel) { this.targetLevel = targetLevel; return this; }
        public DiagnosticTestBuilder nativeLanguage(String nativeLanguage) { this.nativeLanguage = nativeLanguage; return this; }
        public DiagnosticTestBuilder questions(List<DiagnosticQuestion> questions) { this.questions = questions; return this; }
        public DiagnosticTestBuilder currentQuestionIndex(int currentQuestionIndex) { this.currentQuestionIndex = currentQuestionIndex; return this; }
        public DiagnosticTestBuilder correctAnswers(int correctAnswers) { this.correctAnswers = correctAnswers; return this; }
        public DiagnosticTestBuilder completed(boolean completed) { this.completed = completed; return this; }
        public DiagnosticTestBuilder startedAt(long startedAt) { this.startedAt = startedAt; return this; }
        public DiagnosticTestBuilder completedAt(long completedAt) { this.completedAt = completedAt; return this; }
        public DiagnosticTest build() {
            return new DiagnosticTest(
                userId, 
                targetLanguage, 
                targetLevel, 
                nativeLanguage, 
                questions, 
                currentQuestionIndex, 
                correctAnswers, 
                completed, 
                startedAt, 
                completedAt
            );
        }
    }
}