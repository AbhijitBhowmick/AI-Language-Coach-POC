package com.platform.community.controller;

import com.platform.community.dto.*;
import com.platform.community.service.CommunityService;
import com.platform.community.service.LeaderboardService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/community")
@SecurityRequirement(name = "bearerAuth")
public class CommunityController {

    private final CommunityService communityService;
    private final LeaderboardService leaderboardService;

    public CommunityController(CommunityService communityService, LeaderboardService leaderboardService) {
        this.communityService = communityService;
        this.leaderboardService = leaderboardService;
    }

    @PostMapping("/communities")
    public ResponseEntity<CommunityResponseDTO> createCommunity(
            @RequestBody CreateCommunityRequest request,
            @RequestHeader("X-User-Id") UUID userId,
            @RequestHeader("X-Tenant-Id") String tenantId,
            @RequestHeader("X-Role") String role) {
        
        if (!"ADMIN_TEACHER".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403).build();
        }
        
        CommunityResponseDTO response = communityService.createCommunity(request, userId, tenantId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/communities")
    public ResponseEntity<Page<CommunityResponseDTO>> getCommunities(
            @RequestHeader("X-Tenant-Id") String tenantId,
            @RequestHeader("X-User-Id") UUID userId,
            @RequestHeader("X-Role") String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        return ResponseEntity.ok(communityService.getCommunities(tenantId, userId, role, PageRequest.of(page, size)));
    }

    @GetMapping("/communities/{id}")
    public ResponseEntity<CommunityResponseDTO> getCommunity(
            @PathVariable UUID id,
            @RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(communityService.getCommunity(id, userId));
    }

    @DeleteMapping("/communities/{id}")
    public ResponseEntity<Void> deleteCommunity(
            @PathVariable UUID id,
            @RequestHeader("X-User-Id") UUID userId,
            @RequestHeader("X-Role") String role) {
        
        if (!"ADMIN_TEACHER".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403).build();
        }
        
        communityService.deleteCommunity(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/communities/{id}/join")
    public ResponseEntity<CommunityMemberResponseDTO> joinCommunity(
            @PathVariable UUID id,
            @RequestHeader("X-User-Id") UUID userId) {
        
        CommunityMemberResponseDTO response = communityService.joinCommunity(id, userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/communities/{id}/leave")
    public ResponseEntity<Void> leaveCommunity(
            @PathVariable UUID id,
            @RequestHeader("X-User-Id") UUID userId) {
        
        communityService.leaveCommunity(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/communities/{id}/members/{userId}/block")
    public ResponseEntity<Void> blockMember(
            @PathVariable UUID id,
            @PathVariable UUID userId,
            @RequestHeader("X-User-Id") UUID adminTeacherId,
            @RequestHeader("X-Role") String role) {
        
        if (!"ADMIN_TEACHER".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403).build();
        }
        
        communityService.blockMember(id, userId, adminTeacherId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/communities/{id}/members/{userId}/unblock")
    public ResponseEntity<Void> unblockMember(
            @PathVariable UUID id,
            @PathVariable UUID userId,
            @RequestHeader("X-User-Id") UUID adminTeacherId,
            @RequestHeader("X-Role") String role) {
        
        if (!"ADMIN_TEACHER".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403).build();
        }
        
        communityService.unblockMember(id, userId, adminTeacherId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/communities/{id}/members/{userId}/promote")
    public ResponseEntity<CommunityMemberResponseDTO> promoteMember(
            @PathVariable UUID id,
            @PathVariable UUID userId,
            @RequestHeader("X-User-Id") UUID adminTeacherId,
            @RequestHeader("X-Role") String role) {
        
        if (!"ADMIN_TEACHER".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403).build();
        }
        
        CommunityMemberResponseDTO response = communityService.addMemberAsAdmin(id, userId, adminTeacherId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/communities/{id}/members")
    public ResponseEntity<Page<CommunityMemberResponseDTO>> getMembers(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        
        return ResponseEntity.ok(communityService.getMembers(id, PageRequest.of(page, size)));
    }

    @PostMapping("/communities/{id}/posts")
    public ResponseEntity<PostResponseDTO> createPost(
            @PathVariable UUID id,
            @RequestBody CreatePostRequest request,
            @RequestHeader("X-User-Id") UUID userId) {
        
        PostResponseDTO response = communityService.createPost(id, userId, request.getContent());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/communities/{id}/posts")
    public ResponseEntity<Page<PostResponseDTO>> getPosts(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        return ResponseEntity.ok(communityService.getPosts(id, PageRequest.of(page, size)));
    }

    @DeleteMapping("/posts/{id}")
    public ResponseEntity<Void> deletePost(
            @PathVariable UUID id,
            @RequestHeader("X-User-Id") UUID userId,
            @RequestHeader("X-Role") String role) {
        
        communityService.deletePost(id, userId, "ADMIN_TEACHER".equalsIgnoreCase(role));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/posts/{id}/like")
    public ResponseEntity<Void> likePost(
            @PathVariable UUID id,
            @RequestHeader("X-User-Id") UUID userId) {
        
        communityService.likePost(id, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/posts/{id}/comments")
    public ResponseEntity<CommentResponseDTO> addComment(
            @PathVariable UUID id,
            @RequestBody CreateCommentRequest request,
            @RequestHeader("X-User-Id") UUID userId) {
        
        CommentResponseDTO response = communityService.addComment(id, userId, request.getContent());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/posts/{id}/comments")
    public ResponseEntity<Page<CommentResponseDTO>> getComments(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        return ResponseEntity.ok(communityService.getComments(id, PageRequest.of(page, size)));
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<List<LeaderboardEntry>> getGlobalLeaderboard(
            @RequestHeader("X-Tenant-Id") String tenantId,
            @RequestParam(defaultValue = "10") int limit) {
        
        return ResponseEntity.ok(leaderboardService.getGlobalLeaderboard(tenantId, limit));
    }

    @GetMapping("/communities/{id}/leaderboard")
    public ResponseEntity<List<LeaderboardEntry>> getCommunityLeaderboard(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "10") int limit) {
        
        return ResponseEntity.ok(leaderboardService.getCommunityLeaderboard(id, limit));
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, String>> status() {
        return ResponseEntity.ok(Map.of("status", "UP"));
    }
}