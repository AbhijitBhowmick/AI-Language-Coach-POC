package com.platform.community.repository;

import com.platform.community.entity.CommunityMember;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CommunityMemberRepository extends JpaRepository<CommunityMember, UUID> {
    Optional<CommunityMember> findByCommunityIdAndUserId(UUID communityId, UUID userId);
    Page<CommunityMember> findByCommunityIdAndBlockedFalse(UUID communityId, Pageable pageable);
    List<CommunityMember> findByUserIdAndBlockedFalse(UUID userId);
    boolean existsByCommunityIdAndUserIdAndBlockedFalse(UUID communityId, UUID userId);
    boolean existsByCommunityIdAndUserId(UUID communityId, UUID userId);
    void deleteByCommunityIdAndUserId(UUID communityId, UUID userId);
}