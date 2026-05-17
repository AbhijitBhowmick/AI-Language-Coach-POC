package com.platform.community.repository;

import com.platform.community.entity.CommunityComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CommunityCommentRepository extends JpaRepository<CommunityComment, UUID> {
    Page<CommunityComment> findByPostIdOrderByCreatedAtDesc(UUID postId, Pageable pageable);
    long countByPostId(UUID postId);
}