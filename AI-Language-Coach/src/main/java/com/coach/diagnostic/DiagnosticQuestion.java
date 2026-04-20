package com.coach.diagnostic;

import java.util.List;
import java.util.Map;

public class DiagnosticQuestion {

    public enum QuestionType {
        LISTENING_FILL_BLANK,
        VISUAL_MULTIPLE_CHOICE,
        GRAMMAR_COMPLETION,
        DIALOGUE_COMPLETION
    }
    
    private int questionNumber;
    private QuestionType type;
    private String targetLanguage;
    private String targetLevel;
    private String nativeLanguage;
    private String questionText;
    private String audioUrl;
    private String imageUrl;
    private String situation;
    private List<String> options;
    private String correctAnswer;
    private String explanation;
    private String level;
    private Map<String, String> linguisticBridges;

    public DiagnosticQuestion() {}

    public DiagnosticQuestion(int questionNumber, QuestionType type, String targetLanguage, String targetLevel,
                               String nativeLanguage, String questionText, String audioUrl, String imageUrl,
                               String situation, List<String> options, String correctAnswer, String explanation,
                               String level, Map<String, String> linguisticBridges) {
        this.questionNumber = questionNumber;
        this.type = type;
        this.targetLanguage = targetLanguage;
        this.targetLevel = targetLevel;
        this.nativeLanguage = nativeLanguage;
        this.questionText = questionText;
        this.audioUrl = audioUrl;
        this.imageUrl = imageUrl;
        this.situation = situation;
        this.options = options;
        this.correctAnswer = correctAnswer;
        this.explanation = explanation;
        this.level = level;
        this.linguisticBridges = linguisticBridges;
    }

    public static DiagnosticQuestionBuilder builder() {
        return new DiagnosticQuestionBuilder();
    }

    public int getQuestionNumber() { return questionNumber; }
    public void setQuestionNumber(int questionNumber) { this.questionNumber = questionNumber; }
    public QuestionType getType() { return type; }
    public void setType(QuestionType type) { this.type = type; }
    public String getTargetLanguage() { return targetLanguage; }
    public void setTargetLanguage(String targetLanguage) { this.targetLanguage = targetLanguage; }
    public String getTargetLevel() { return targetLevel; }
    public void setTargetLevel(String targetLevel) { this.targetLevel = targetLevel; }
    public String getNativeLanguage() { return nativeLanguage; }
    public void setNativeLanguage(String nativeLanguage) { this.nativeLanguage = nativeLanguage; }
    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }
    public String getAudioUrl() { return audioUrl; }
    public void setAudioUrl(String audioUrl) { this.audioUrl = audioUrl; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getSituation() { return situation; }
    public void setSituation(String situation) { this.situation = situation; }
    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }
    public String getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }
    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
    public Map<String, String> getLinguisticBridges() { return linguisticBridges; }
    public void setLinguisticBridges(Map<String, String> linguisticBridges) { this.linguisticBridges = linguisticBridges; }

    public String getLinguisticBridge(String nativeLanguage) {
        if (linguisticBridges != null && nativeLanguage != null) {
            return linguisticBridges.get(nativeLanguage);
        }
        return null;
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
        public DiagnosticQuestionBuilder linguisticBridge(String bridgeText) {
            this.linguisticBridges.put("en", bridgeText);
            return this;
        }
        public DiagnosticQuestion build() {
            return new DiagnosticQuestion(questionNumber, type, targetLanguage, targetLevel, nativeLanguage,
                    questionText, audioUrl, imageUrl, situation, options, correctAnswer, explanation, level, linguisticBridges);
        }
    }
}