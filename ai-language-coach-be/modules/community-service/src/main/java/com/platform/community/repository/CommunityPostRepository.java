package com.platform.community.repository;

import com.platform.community.entity.CommunityPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CommunityPostRepository extends JpaRepository<CommunityPost, UUID> {
    Page<CommunityPost> findByCommunityIdOrderByCreatedAtDesc(UUID communityId, Pageable pageable);
    Page<CommunityPost> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
}