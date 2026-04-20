package com.coach.diagnostic;

import com.coach.common.LearningContext;
import com.coach.profile.ProfileService;
import com.coach.profile.UserProfile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/diagnostic")
public class DiagnosticController {

    private final DiagnosticService diagnosticService;
    private final ProfileService profileService;

    public DiagnosticController(DiagnosticService diagnosticService, ProfileService profileService) {
        this.diagnosticService = diagnosticService;
        this.profileService = profileService;
    }

    @PostMapping("/start")
    public ResponseEntity<DiagnosticTest> startTest(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String targetLanguage,
            @RequestParam(required = false) String targetLevel,
            @RequestParam(required = false) String nativeLanguage) {
        
        UUID userId = extractUserId(userDetails);
        
        LearningContext context = resolveContext(userId, targetLanguage, targetLevel, nativeLanguage);
        DiagnosticTest test = diagnosticService.startTest(userId, context);
        return ResponseEntity.ok(test);
    }

    @GetMapping("/question")
    public ResponseEntity<DiagnosticQuestion> getCurrentQuestion(
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = extractUserId(userDetails);
        DiagnosticQuestion question = diagnosticService.getCurrentQuestion(userId);
        
        if (question == null) {
            return ResponseEntity.noContent().build();
        }
        
        return ResponseEntity.ok(question);
    }

    @PostMapping("/answer")
    public ResponseEntity<DiagnosticTest> submitAnswer(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody AnswerSubmission submission) {
        UUID userId = extractUserId(userDetails);
        DiagnosticTest test = diagnosticService.submitAnswer(userId, submission);
        return ResponseEntity.ok(test);
    }

    @GetMapping("/result")
    public ResponseEntity<TestResult> getTestResult(
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = extractUserId(userDetails);
        TestResult result = diagnosticService.getTestResult(userId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/status")
    public ResponseEntity<DiagnosticTest> getTestStatus(
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = extractUserId(userDetails);
        DiagnosticTest test = diagnosticService.getTest(userId);
        
        if (test == null) {
            return ResponseEntity.noContent().build();
        }
        
        return ResponseEntity.ok(test);
    }

    private LearningContext resolveContext(UUID userId, String targetLanguage, String targetLevel, String nativeLanguage) {
        UserProfile profile = profileService.getProfile(userId);
        
        if (profile != null && profile.getContext() != null) {
            LearningContext saved = profile.getContext();
            return new LearningContext(
                targetLanguage != null ? targetLanguage : saved.targetLanguage(),
                targetLevel != null ? targetLevel : saved.targetLevel(),
                nativeLanguage != null ? nativeLanguage : saved.nativeLanguage()
            );
        }
        
        return new LearningContext(
            targetLanguage != null ? targetLanguage : "Czech",
            targetLevel != null ? targetLevel : "A1",
            nativeLanguage != null ? nativeLanguage : "en"
        );
    }

    private UUID extractUserId(UserDetails userDetails) {
        return UUID.fromString(userDetails.getUsername());
    }
}