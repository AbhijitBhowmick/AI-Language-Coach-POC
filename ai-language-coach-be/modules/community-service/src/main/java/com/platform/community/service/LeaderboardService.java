package com.platform.community.service;

import com.platform.community.dto.LeaderboardEntry;
import com.platform.community.entity.Community;
import com.platform.community.entity.CommunityActivity;
import com.platform.community.repository.CommunityActivityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class LeaderboardService {

    private static final Logger log = LoggerFactory.getLogger(LeaderboardService.class);

    private final CommunityActivityRepository activityRepository;

    public LeaderboardService(CommunityActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    public List<LeaderboardEntry> getGlobalLeaderboard(String tenantId, int limit) {
        LocalDateTime since = LocalDateTime.now().minusMonths(1);
        List<Object[]> results = activityRepository.findTopUsersByScore(tenantId, since);
        
        return results.stream()
            .limit(limit)
            .map(r -> new LeaderboardEntry((UUID) r[0], ((Number) r[1]).longValue()))
            .collect(Collectors.toList());
    }

    public List<LeaderboardEntry> getCommunityLeaderboard(UUID communityId, int limit) {
        LocalDateTime since = LocalDateTime.now().minusMonths(1);
        List<Object[]> results = activityRepository.findTopUsersByScoreInCommunity(communityId, since);
        
        return results.stream()
            .limit(limit)
            .map(r -> new LeaderboardEntry((UUID) r[0], ((Number) r[1]).longValue()))
            .collect(Collectors.toList());
    }

    public void recordVoiceActivity(UUID userId, String tenantId, Community community) {
        activityRepository.save(new CommunityActivity(
            userId, tenantId, CommunityActivity.ActivityType.VOICE_SESSION, 50, community
        ));
        log.info("Recorded voice activity for user {}", userId);
    }

    public void recordGameActivity(UUID userId, String tenantId, Community community) {
        activityRepository.save(new CommunityActivity(
            userId, tenantId, CommunityActivity.ActivityType.GAME_COMPLETE, 30, community
        ));
        log.info("Recorded game activity for user {}", userId);
    }

    public void recordDiagnosticActivity(UUID userId, String tenantId, Community community) {
        activityRepository.save(new CommunityActivity(
            userId, tenantId, CommunityActivity.ActivityType.DIAGNOSTIC_COMPLETE, 20, community
        ));
        log.info("Recorded diagnostic activity for user {}", userId);
    }
}