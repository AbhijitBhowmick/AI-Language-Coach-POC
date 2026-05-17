package com.platform.diagnostic.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record DiagnosticQuestion(
        int questionNumber,
        QuestionType type,
        String targetLanguage,
        String targetLevel,
        String nativeLanguage,
        String questionText,
        String audioUrl,
        String imageUrl,
        String situation,
        List<String> options,
        String correctAnswer,
        String explanation,
        String level,
        Map<String, String> linguisticBridges
) implements Serializable {
    public enum QuestionType {
        LISTENING_FILL_BLANK,
        VISUAL_MULTIPLE_CHOICE,
        GRAMMAR_COMPLETION,
        DIALOGUE_COMPLETION
    }

    public DiagnosticQuestion {
        if (targetLanguage == null) targetLanguage = "Czech";
        if (targetLevel == null) targetLevel = "A1";
        if (nativeLanguage == null) nativeLanguage = "en";
        if (level == null) level = "A1";
        if (linguisticBridges == null) linguisticBridges = Map.of();
    }

    public String getLinguisticBridge(String nativeLanguage) {
        if (linguisticBridges != null && nativeLanguage != null) {
            return linguisticBridges.get(nativeLanguage);
        }
        return null;
    }

    public int getQuestionNumber() { return questionNumber; }
    public QuestionType getType() { return type; }
    public String getTargetLanguage() { return targetLanguage; }
    public String getTargetLevel() { return targetLevel; }
    public String getNativeLanguage() { return nativeLanguage; }
    public String getQuestionText() { return questionText; }
    public String getAudioUrl() { return audioUrl; }
    public String getImageUrl() { return imageUrl; }
    public String getSituation() { return situation; }
    public List<String> getOptions() { return options; }
    public String getCorrectAnswer() { return correctAnswer; }
    public String getExplanation() { return explanation; }
    public String getLevel() { return level; }
    public Map<String, String> getLinguisticBridges() { return linguisticBridges; }

    public static DiagnosticQuestionBuilder builder() {
        return new DiagnosticQuestionBuilder();
    }

    public static class DiagnosticQuestionBuilder {
        private int questionNumber;
        private QuestionType type;
        private String targetLanguage = "Czech";
        private String targetLevel = "A1";
        private String nativeLanguage = "en";
        private String questionText;
        private String audioUrl;
        private String imageUrl;
        private String situation;
        private List<String> options;
        private String correctAnswer;
        private String explanation;
        private String level = "A1";
        private java.util.Map<String, String> linguisticBridges = new java.util.HashMap<>();

        public DiagnosticQuestionBuilder questionNumber(int questionNumber) { this.questionNumber = questionNumber; return this; }
        public DiagnosticQuestionBuilder type(QuestionType type) { this.type = type; return this; }
        public DiagnosticQuestionBuilder targetLanguage(String targetLanguage) { this.targetLanguage = targetLanguage; return this; }
        public DiagnosticQuestionBuilder targetLevel(String targetLevel) { this.targetLevel = targetLevel; return this; }
        public DiagnosticQuestionBuilder nativeLanguage(String nativeLanguage) { this.nativeLanguage = nativeLanguage; return this; }
        public DiagnosticQuestionBuilder questionText(String questionText) { this.questionText = questionText; return this; }
        public DiagnosticQuestionBuilder audioUrl(String audioUrl) { this.audioUrl = audioUrl; return this; }
        public DiagnosticQuestionBuilder imageUrl(String imageUrl) { this.imageUrl = imageUrl; return this; }
        public DiagnosticQuestionBuilder situation(String situation) { this.situation = situation; return this; }
        public DiagnosticQuestionBuilder options(List<String> options) { this.options = options; return this; }
        public DiagnosticQuestionBuilder correctAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; return this; }
        public DiagnosticQuestionBuilder explanation(String explanation) { this.explanation = explanation; return this; }
        public DiagnosticQuestionBuilder level(String level) { this.level = level; return this; }
        public DiagnosticQuestionBuilder linguisticBridge(String nativeLang, String bridgeText) {
            this.linguisticBridges.put(nativeLang, bridgeText);
            return this;
        }
        public DiagnosticQuestion build() {
            return new DiagnosticQuestion(questionNumber, type, targetLanguage, targetLevel, nativeLanguage,
                    questionText, audioUrl, imageUrl, situation, options, correctAnswer, explanation, level, linguisticBridges);
        }
    }
}