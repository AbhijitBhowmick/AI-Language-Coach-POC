package com.platform.diagnostic.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "game_templates")
public class GameTemplateEntity {

    @Id
    @Column(name = "template_id", length = 50)
    private String templateId;

    @Column(nullable = false, length = 100)
    private String displayName;

    @Column(name = "template_category", nullable = false, length = 30)
    private String templateCategory;

    @Column(name = "display_order")
    private Integer displayOrder = 0;

    @Column(name = "icon_class", length = 50)
    private String iconClass;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "default_time_seconds")
    private Integer defaultTimeSeconds = 30;

    @Column(name = "points_per_correct")
    private Integer pointsPerCorrect = 10;

    @Column(name = "penalty_points")
    private Integer penaltyPoints = 0;

    @Column(name = "lives_enabled")
    private Boolean livesEnabled = false;

    @Column(name = "branching_enabled")
    private Boolean branchingEnabled = false;

    @Column(name = "min_questions")
    private Integer minQuestions = 4;

    @Column(name = "max_questions")
    private Integer maxQuestions = 20;

    @Column(name = "config_schema", columnDefinition = "JSONB")
    @JdbcTypeCode(SqlTypes.JSON)
    private String configSchema;

    @Column(name = "difficulty_weights", columnDefinition = "JSONB")
    @JdbcTypeCode(SqlTypes.JSON)
    private String difficultyWeights;

    @Column(name = "skill_areas", columnDefinition = "JSONB")
    @JdbcTypeCode(SqlTypes.JSON)
    private String skillAreas;

    @Column
    private Boolean active = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public String getTemplateId() { return templateId; }
    public void setTemplateId(String templateId) { this.templateId = templateId; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getTemplateCategory() { return templateCategory; }
    public void setTemplateCategory(String templateCategory) { this.templateCategory = templateCategory; }
    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
    public String getIconClass() { return iconClass; }
    public void setIconClass(String iconClass) { this.iconClass = iconClass; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getDefaultTimeSeconds() { return defaultTimeSeconds; }
    public void setDefaultTimeSeconds(Integer defaultTimeSeconds) { this.defaultTimeSeconds = defaultTimeSeconds; }
    public Integer getPointsPerCorrect() { return pointsPerCorrect; }
    public void setPointsPerCorrect(Integer pointsPerCorrect) { this.pointsPerCorrect = pointsPerCorrect; }
    public Integer getPenaltyPoints() { return penaltyPoints; }
    public void setPenaltyPoints(Integer penaltyPoints) { this.penaltyPoints = penaltyPoints; }
    public Boolean getLivesEnabled() { return livesEnabled; }
    public void setLivesEnabled(Boolean livesEnabled) { this.livesEnabled = livesEnabled; }
    public Boolean getBranchingEnabled() { return branchingEnabled; }
    public void setBranchingEnabled(Boolean branchingEnabled) { this.branchingEnabled = branchingEnabled; }
    public Integer getMinQuestions() { return minQuestions; }
    public void setMinQuestions(Integer minQuestions) { this.minQuestions = minQuestions; }
    public Integer getMaxQuestions() { return maxQuestions; }
    public void setMaxQuestions(Integer maxQuestions) { this.maxQuestions = maxQuestions; }
    public String getConfigSchema() { return configSchema; }
    public void setConfigSchema(String configSchema) { this.configSchema = configSchema; }
    public String getDifficultyWeights() { return difficultyWeights; }
    public void setDifficultyWeights(String difficultyWeights) { this.difficultyWeights = difficultyWeights; }
    public String getSkillAreas() { return skillAreas; }
    public void setSkillAreas(String skillAreas) { this.skillAreas = skillAreas; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}