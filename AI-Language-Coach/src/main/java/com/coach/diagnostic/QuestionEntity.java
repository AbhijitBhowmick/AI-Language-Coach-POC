package com.coach.diagnostic;

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
    private String targetLanguage;

    @Column(nullable = false)
    private String targetLevel;

    @Column
    private String nativeLanguage;

    @Column(nullable = false)
    private String questionText;

    @Column(columnDefinition = "TEXT")
    private String situation;

    @Column(columnDefinition = "TEXT")
    private String options;

    @Column(nullable = false)
    private String correctAnswer;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    @Column
    private String questionType;

    @Column(columnDefinition = "TEXT")
    private String linguisticBridgeEn;

    @Column(columnDefinition = "TEXT")
    private String linguisticBridgeBn;

    @Column(columnDefinition = "TEXT")
    private String linguisticBridgeHi;

    @Column(columnDefinition = "TEXT")
    private String linguisticBridgeTe;

    @Column(columnDefinition = "TEXT")
    private String linguisticBridgeUk;

    @Column
    private boolean active;

    @Column
    private int displayOrder;

    @Column
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    public QuestionEntity() {}

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getTargetLanguage() { return targetLanguage; }
    public void setTargetLanguage(String targetLanguage) { this.targetLanguage = targetLanguage; }
    public String getTargetLevel() { return targetLevel; }
    public void setTargetLevel(String targetLevel) { this.targetLevel = targetLevel; }
    public String getNativeLanguage() { return nativeLanguage; }
    public void setNativeLanguage(String nativeLanguage) { this.nativeLanguage = nativeLanguage; }
    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }
    public String getSituation() { return situation; }
    public void setSituation(String situation) { this.situation = situation; }
    public String getOptions() { return options; }
    public void setOptions(String options) { this.options = options; }
    public String getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }
    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
    public String getQuestionType() { return questionType; }
    public void setQuestionType(String questionType) { this.questionType = questionType; }
    public String getLinguisticBridgeEn() { return linguisticBridgeEn; }
    public void setLinguisticBridgeEn(String linguisticBridgeEn) { this.linguisticBridgeEn = linguisticBridgeEn; }
    public String getLinguisticBridgeBn() { return linguisticBridgeBn; }
    public void setLinguisticBridgeBn(String linguisticBridgeBn) { this.linguisticBridgeBn = linguisticBridgeBn; }
    public String getLinguisticBridgeHi() { return linguisticBridgeHi; }
    public void setLinguisticBridgeHi(String linguisticBridgeHi) { this.linguisticBridgeHi = linguisticBridgeHi; }
    public String getLinguisticBridgeTe() { return linguisticBridgeTe; }
    public void setLinguisticBridgeTe(String linguisticBridgeTe) { this.linguisticBridgeTe = linguisticBridgeTe; }
    public String getLinguisticBridgeUk() { return linguisticBridgeUk; }
    public void setLinguisticBridgeUk(String linguisticBridgeUk) { this.linguisticBridgeUk = linguisticBridgeUk; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public int getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}