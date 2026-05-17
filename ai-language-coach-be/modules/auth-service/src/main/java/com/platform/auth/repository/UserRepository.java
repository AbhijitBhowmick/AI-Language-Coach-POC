package com.platform.auth.repository;

import com.platform.auth.entity.User;
import com.platform.auth.entity.User.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmailAndTenantId(String email, String tenantId);
    boolean existsByEmailAndTenantId(String email, String tenantId);
    
    List<User> findByTenantId(String tenantId);
    
    List<User> findByTenantIdAndStatus(String tenantId, UserStatus status);
    
    @Query("SELECT u FROM User u WHERE u.tenantId = :tenantId AND u.status = 'PENDING_APPROVAL'")
    List<User> findPendingApprovals(String tenantId);
    
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r = 'BUSINESS_ADMIN' AND u.status = 'PENDING_APPROVAL'")
    List<User> findPendingBusinessAdmins();
    
    boolean existsByEmailAndTenantIdAndIdNot(String email, String tenantId, UUID id);
}