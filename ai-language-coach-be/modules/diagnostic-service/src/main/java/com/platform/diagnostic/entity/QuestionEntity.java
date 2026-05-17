package com.platform.diagnostic.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "diagnostic_questions")
public class QuestionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String questionText;

    @Column(nullable = false)
    private String targetLanguage;

    @Column(nullable = false)
    private String targetLevel;

    @Column(columnDefinition = "TEXT")
    private String options;

    @Column(nullable = false)
    private String correctAnswer;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    private String nativeLanguage;
    private String situation;
    private String questionType;
    private Integer displayOrder;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String linguisticBridgeEn;
    private String linguisticBridgeBn;
    private String linguisticBridgeHi;
    private String linguisticBridgeTe;
    private String linguisticBridgeUk;
    private String templateType;
    private Double difficultyWeight;
    private String skillArea;
    private Integer timeLimitSeconds;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }
    public String getTargetLanguage() { return targetLanguage; }
    public void setTargetLanguage(String targetLanguage) { this.targetLanguage = targetLanguage; }
    public String getTargetLevel() { return targetLevel; }
    public void setTargetLevel(String targetLevel) { this.targetLevel = targetLevel; }
    public String getOptions() { return options; }
    public void setOptions(String options) { this.options = options; }
    public String getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }
    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
    public String getNativeLanguage() { return nativeLanguage; }
    public void setNativeLanguage(String nativeLanguage) { this.nativeLanguage = nativeLanguage; }
    public String getSituation() { return situation; }
    public void setSituation(String situation) { this.situation = situation; }
    public String getQuestionType() { return questionType; }
    public void setQuestionType(String questionType) { this.questionType = questionType; }
    public String getLinguisticBridgeEn() { return linguisticBridgeEn; }
    public void setLinguisticBridgeEn(String linguisticBridgeEn) { this.linguisticBridgeEn = linguisticBridgeEn; }
    public String getTemplateType() { return templateType; }
    public void setTemplateType(String templateType) { this.templateType = templateType; }
    public Double getDifficultyWeight() { return difficultyWeight; }
    public void setDifficultyWeight(Double difficultyWeight) { this.difficultyWeight = difficultyWeight; }
    public String getSkillArea() { return skillArea; }
    public void setSkillArea(String skillArea) { this.skillArea = skillArea; }
    public Integer getTimeLimitSeconds() { return timeLimitSeconds; }
    public void setTimeLimitSeconds(Integer timeLimitSeconds) { this.timeLimitSeconds = timeLimitSeconds; }
}