package com.platform.profile.controller;

import com.platform.profile.dto.*;
import com.platform.profile.service.ProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/profile")
@SecurityRequirement(name = "bearerAuth")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @PostMapping
    public ResponseEntity<UserProfileResponse> createProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody CreateProfileRequest request) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        return ResponseEntity.ok(profileService.createProfile(userId, userDetails.getUsername(), request));
    }

    @GetMapping
    public ResponseEntity<UserProfileResponse> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        return ResponseEntity.ok(profileService.getProfile(userId));
    }

    @PutMapping
    public ResponseEntity<UserProfileResponse> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UpdateProfileRequest request) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        return ResponseEntity.ok(profileService.updateProfile(userId, request));
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteProfile(@AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        profileService.deleteProfile(userId);
        return ResponseEntity.noContent().build();
    }
}