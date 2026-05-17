package com.platform.community.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "community_members", 
    uniqueConstraints = @UniqueConstraint(columnNames = {"community_id", "user_id"}))
@EntityListeners(AuditingEntityListener.class)
public class CommunityMember {

    public enum MemberRole {
        ADMIN,
        MEMBER
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "community_id", nullable = false)
    private Community community;

    @Column(nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberRole role = MemberRole.MEMBER;

    @Column(nullable = false)
    private boolean blocked = false;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    public CommunityMember() {}

    public CommunityMember(Community community, UUID userId, MemberRole role) {
        this.community = community;
        this.userId = userId;
        this.role = role;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Community getCommunity() { return community; }
    public void setCommunity(Community community) { this.community = community; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public MemberRole getRole() { return role; }
    public void setRole(MemberRole role) { this.role = role; }
    public boolean isBlocked() { return blocked; }
    public void setBlocked(boolean blocked) { this.blocked = blocked; }
    public LocalDateTime getJoinedAt() { return joinedAt; }
    public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }
}