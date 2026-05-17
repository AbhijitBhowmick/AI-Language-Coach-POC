package com.platform.auth.service;

import com.platform.auth.dto.*;
import com.platform.auth.entity.ApprovalRequest;
import com.platform.auth.entity.ApprovalRequest.ApprovalStatus;
import com.platform.auth.entity.Role;
import com.platform.auth.entity.User;
import com.platform.auth.entity.User.UserStatus;
import com.platform.auth.repository.ApprovalRequestRepository;
import com.platform.auth.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserManagementService {

    private static final Logger log = LoggerFactory.getLogger(UserManagementService.class);

    private final UserRepository userRepository;
    private final ApprovalRequestRepository approvalRequestRepository;

    public UserManagementService(UserRepository userRepository, 
                                  ApprovalRequestRepository approvalRequestRepository) {
        this.userRepository = userRepository;
        this.approvalRequestRepository = approvalRequestRepository;
    }

    @Transactional
    public UserResponse approveUser(UUID userId, UUID approverId, String comment) {
        User approver = userRepository.findById(approverId)
                .orElseThrow(() -> new RuntimeException("Approver not found"));
        
        Role approverRole = getPrimaryRole(approver);
        if (approverRole != Role.SUPER_ADMIN && approverRole != Role.BUSINESS_ADMIN) {
            throw new RuntimeException("Only SUPER_ADMIN or BUSINESS_ADMIN can approve users");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setStatus(UserStatus.APPROVED);
        user.setApprovedBy(approverId);
        user.setApprovedAt(java.time.LocalDateTime.now());
        user.setApprovalComment(comment);
        user.setEnabled(true);
        
        userRepository.save(user);

        ApprovalRequest approvalRequest = approvalRequestRepository
                .findFirstByUserIdAndStatusOrderByCreatedAtDesc(userId, ApprovalStatus.PENDING)
                .orElse(null);
        
        if (approvalRequest != null) {
            approvalRequest.approve(approverId);
            approvalRequestRepository.save(approvalRequest);
        }

        log.info("User {} approved by {}", user.getEmail(), approver.getEmail());
        
        return toUserResponse(user);
    }

    @Transactional
    public UserResponse rejectUser(UUID userId, UUID rejecterId, String reason) {
        User rejecter = userRepository.findById(rejecterId)
                .orElseThrow(() -> new RuntimeException("Rejecter not found"));
        
        Role rejecterRole = getPrimaryRole(rejecter);
        if (rejecterRole != Role.SUPER_ADMIN && rejecterRole != Role.BUSINESS_ADMIN) {
            throw new RuntimeException("Only SUPER_ADMIN or BUSINESS_ADMIN can reject users");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setStatus(UserStatus.REJECTED);
        user.setEnabled(false);
        userRepository.save(user);

        ApprovalRequest approvalRequest = approvalRequestRepository
                .findFirstByUserIdAndStatusOrderByCreatedAtDesc(userId, ApprovalStatus.PENDING)
                .orElse(null);
        
        if (approvalRequest != null) {
            approvalRequest.reject(rejecterId, reason);
            approvalRequestRepository.save(approvalRequest);
        }

        log.info("User {} rejected by {}: {}", user.getEmail(), rejecter.getEmail(), reason);
        
        return toUserResponse(user);
    }

    public List<UserResponse> getPendingApprovals(String tenantId) {
        return userRepository.findByTenantIdAndStatus(tenantId, UserStatus.PENDING_APPROVAL)
                .stream()
                .map(this::toUserResponse)
                .collect(Collectors.toList());
    }

    public List<UserResponse> getAllUsers(String tenantId) {
        return userRepository.findByTenantId(tenantId)
                .stream()
                .map(this::toUserResponse)
                .collect(Collectors.toList());
    }

    public UserResponse getUserById(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return toUserResponse(user);
    }

    @Transactional
    public UserResponse suspendUser(UUID userId, UUID suspendedBy) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setStatus(UserStatus.SUSPENDED);
        user.setEnabled(false);
        userRepository.save(user);
        
        log.info("User {} suspended by {}", user.getEmail(), suspendedBy);
        
        return toUserResponse(user);
    }

    @Transactional
    public UserResponse activateUser(UUID userId, UUID activatedBy) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setStatus(UserStatus.ACTIVE);
        user.setEnabled(true);
        userRepository.save(user);
        
        log.info("User {} activated by {}", user.getEmail(), activatedBy);
        
        return toUserResponse(user);
    }

    private Role getPrimaryRole(User user) {
        return user.getRoles() != null && !user.getRoles().isEmpty() 
            ? user.getRoles().iterator().next() 
            : Role.USER_STUDENT;
    }

    public UserResponse toUserResponse(User user) {
        Role primaryRole = user.getRoles() != null && !user.getRoles().isEmpty()
            ? user.getRoles().iterator().next()
            : Role.USER_STUDENT;
        
        return new UserResponse(
            user.getId(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.getProvider(),
            user.getTenantId(),
            user.getRoles(),
            user.getStatus(),
            user.getBusinessName(),
            user.getCreatedBy(),
            user.getCreatedAt(),
            user.getApprovedAt()
        );
    }
}