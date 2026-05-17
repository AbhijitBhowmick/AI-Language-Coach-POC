package com.platform.community.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "community_activities")
@EntityListeners(AuditingEntityListener.class)
public class CommunityActivity {

    public enum ActivityType {
        VOICE_SESSION,
        GAME_COMPLETE,
        DIAGNOSTIC_COMPLETE,
        POST_CREATED,
        POST_LIKED,
        COMMENT_CREATED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String tenantId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityType type;

    @Column(nullable = false)
    private int score;

    @Column
    private String metadata;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "community_id")
    private Community community;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public CommunityActivity() {}

    public CommunityActivity(UUID userId, String tenantId, ActivityType type, int score, Community community) {
        this.userId = userId;
        this.tenantId = tenantId;
        this.type = type;
        this.score = score;
        this.community = community;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public ActivityType getType() { return type; }
    public void setType(ActivityType type) { this.type = type; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }
    public Community getCommunity() { return community; }
    public void setCommunity(Community community) { this.community = community; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}