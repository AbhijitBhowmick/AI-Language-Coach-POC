package com.platform.community.repository;

import com.platform.community.entity.Community;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CommunityRepository extends JpaRepository<Community, UUID> {
    Page<Community> findByTenantIdAndIsActiveTrue(String tenantId, Pageable pageable);
    List<Community> findByAdminTeacherId(UUID adminTeacherId);
    boolean existsByIdAndAdminTeacherId(UUID communityId, UUID adminTeacherId);
}