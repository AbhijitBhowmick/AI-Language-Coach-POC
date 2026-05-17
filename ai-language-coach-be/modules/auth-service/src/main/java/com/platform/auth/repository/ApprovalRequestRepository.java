package com.platform.auth.repository;

import com.platform.auth.entity.ApprovalRequest;
import com.platform.auth.entity.ApprovalRequest.ApprovalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApprovalRequestRepository extends JpaRepository<ApprovalRequest, UUID> {

    List<ApprovalRequest> findByStatus(ApprovalStatus status);

    List<ApprovalRequest> findByTenantIdAndStatus(String tenantId, ApprovalStatus status);

    List<ApprovalRequest> findByUserId(UUID userId);

    Optional<ApprovalRequest> findByUserIdAndStatus(UUID userId, ApprovalStatus status);

    Optional<ApprovalRequest> findFirstByUserIdAndStatusOrderByCreatedAtDesc(UUID userId, ApprovalStatus status);
}