package com.platform.auth.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "approval_requests")
public class ApprovalRequest {

    public enum ApprovalStatus {
        PENDING,
        APPROVED,
        REJECTED,
        CANCELLED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "requested_role", nullable = false)
    private String requestedRole;

    @Column(name = "requested_by", nullable = false)
    private UUID requestedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApprovalStatus status = ApprovalStatus.PENDING;

    @Column(name = "approved_by")
    private UUID approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "tenant_id")
    private String tenantId;

    public ApprovalRequest() {}

    public ApprovalRequest(UUID userId, String requestedRole, UUID requestedBy, String tenantId) {
        this.userId = userId;
        this.requestedRole = requestedRole;
        this.requestedBy = requestedBy;
        this.tenantId = tenantId;
        this.status = ApprovalStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public void approve(UUID approvedBy) {
        this.status = ApprovalStatus.APPROVED;
        this.approvedBy = approvedBy;
        this.approvedAt = LocalDateTime.now();
    }

    public void reject(UUID rejectedBy, String reason) {
        this.status = ApprovalStatus.REJECTED;
        this.approvedBy = rejectedBy;
        this.rejectionReason = reason;
        this.approvedAt = LocalDateTime.now();
    }

    public void cancel() {
        this.status = ApprovalStatus.CANCELLED;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getRequestedRole() { return requestedRole; }
    public void setRequestedRole(String requestedRole) { this.requestedRole = requestedRole; }
    public UUID getRequestedBy() { return requestedBy; }
    public void setRequestedBy(UUID requestedBy) { this.requestedBy = requestedBy; }
    public ApprovalStatus getStatus() { return status; }
    public void setStatus(ApprovalStatus status) { this.status = status; }
    public UUID getApprovedBy() { return approvedBy; }
    public void setApprovedBy(UUID approvedBy) { this.approvedBy = approvedBy; }
    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
}