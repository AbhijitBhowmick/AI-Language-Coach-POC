package com.platform.community.repository;

import com.platform.community.entity.CommunityActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface CommunityActivityRepository extends JpaRepository<CommunityActivity, UUID> {
    
    @Query("SELECT a.userId, SUM(a.score) FROM CommunityActivity a " +
           "WHERE a.tenantId = :tenantId AND a.createdAt >= :since " +
           "GROUP BY a.userId ORDER BY SUM(a.score) DESC")
    List<Object[]> findTopUsersByScore(@Param("tenantId") String tenantId, 
                                         @Param("since") LocalDateTime since);
    
    @Query("SELECT a.userId, SUM(a.score) FROM CommunityActivity a " +
           "WHERE a.community.id = :communityId AND a.createdAt >= :since " +
           "GROUP BY a.userId ORDER BY SUM(a.score) DESC")
    List<Object[]> findTopUsersByScoreInCommunity(@Param("communityId") UUID communityId, 
                                                   @Param("since") LocalDateTime since);
    
    List<CommunityActivity> findByUserIdAndCreatedAtAfter(UUID userId, LocalDateTime since);
}