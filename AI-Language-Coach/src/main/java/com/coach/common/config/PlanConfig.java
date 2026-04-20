package com.coach.common.config;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "plan_types")
public class PlanConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String planCode;

    @Column(nullable = false)
    private String planName;

    @Column
    private Integer requestsPerMinute;

    @Column
    private Integer voiceMinutes;

    @Column
    private String llmModel;

    @Column
    private boolean enabled;

    @Column
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    public PlanConfig() {}

    public PlanConfig(String planCode, String planName, int requestsPerMinute, int voiceMinutes, String llmModel) {
        this.planCode = planCode;
        this.planName = planName;
        this.requestsPerMinute = requestsPerMinute;
        this.voiceMinutes = voiceMinutes;
        this.llmModel = llmModel;
        this.enabled = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getPlanCode() { return planCode; }
    public void setPlanCode(String planCode) { this.planCode = planCode; }
    public String getPlanName() { return planName; }
    public void setPlanName(String planName) { this.planName = planName; }
    public Integer getRequestsPerMinute() { return requestsPerMinute; }
    public void setRequestsPerMinute(Integer requestsPerMinute) { this.requestsPerMinute = requestsPerMinute; }
    public Integer getVoiceMinutes() { return voiceMinutes; }
    public void setVoiceMinutes(Integer voiceMinutes) { this.voiceMinutes = voiceMinutes; }
    public String getLlmModel() { return llmModel; }
    public void setLlmModel(String llmModel) { this.llmModel = llmModel; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}