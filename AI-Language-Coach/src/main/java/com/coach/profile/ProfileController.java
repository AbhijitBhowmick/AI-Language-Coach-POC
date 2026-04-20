package com.coach.profile;

import com.coach.common.LearningContext;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @PostMapping
    public ResponseEntity<UserProfile> createProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String targetLanguage,
            @RequestParam(required = false) String targetLevel,
            @RequestParam(required = false) String nativeLanguage,
            @RequestParam(defaultValue = "FREE") PlanType planType) {
        
        UUID userId = UUID.fromString(extractUserId(userDetails));
        String email = userDetails.getUsername();
        
        LearningContext context = new LearningContext(
            targetLanguage != null ? targetLanguage : "Czech",
            targetLevel != null ? targetLevel : "A1",
            nativeLanguage != null ? nativeLanguage : "en"
        );
        
        UserProfile profile = profileService.createProfile(userId, email, context, planType);
        return ResponseEntity.ok(profile);
    }

    @GetMapping
    public ResponseEntity<UserProfile> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(extractUserId(userDetails));
        UserProfile profile = profileService.getProfile(userId);
        
        if (profile == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(profile);
    }

    @PutMapping
    public ResponseEntity<UserProfile> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody ProfileUpdateRequest request) {
        
        UUID userId = UUID.fromString(extractUserId(userDetails));
        UserProfile profile = profileService.updateProfile(userId, request);
        return ResponseEntity.ok(profile);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteProfile(@AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(extractUserId(userDetails));
        profileService.deleteProfile(userId);
        return ResponseEntity.noContent().build();
    }

    private String extractUserId(UserDetails userDetails) {
        return userDetails.getUsername();
    }
}