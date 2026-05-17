package com.platform.diagnostic.controller;

import com.platform.common.model.LearningContext;
import com.platform.diagnostic.dto.AnswerSubmission;
import com.platform.diagnostic.dto.DiagnosticQuestion;
import com.platform.diagnostic.dto.DiagnosticTest;
import com.platform.diagnostic.dto.TestResult;
import com.platform.diagnostic.service.DiagnosticService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/diagnostic")
@SecurityRequirement(name = "bearerAuth")
public class DiagnosticController {

    private final DiagnosticService diagnosticService;

    public DiagnosticController(DiagnosticService diagnosticService) {
        this.diagnosticService = diagnosticService;
    }

    private UUID extractUserId(UserDetails userDetails) {
        if (userDetails == null) return null;
        try {
            return UUID.fromString(userDetails.getUsername());
        } catch (Exception e) {
            return null;
        }
    }

    @PostMapping("/start")
    public ResponseEntity<DiagnosticTest> startTest(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String targetLanguage,
            @RequestParam(required = false) String targetLevel) {
        UUID userId = extractUserId(userDetails);
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        LearningContext context = new LearningContext(
            targetLanguage != null ? targetLanguage : "Czech",
            targetLevel != null ? targetLevel : "A1",
            null
        );
        return ResponseEntity.ok(diagnosticService.startTest(userId, context));
    }

    @GetMapping("/question")
    public ResponseEntity<DiagnosticQuestion> getCurrentQuestion(@AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = extractUserId(userDetails);
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        DiagnosticQuestion question = diagnosticService.getCurrentQuestion(userId);
        return question != null ? ResponseEntity.ok(question) : ResponseEntity.notFound().build();
    }

    @PostMapping("/answer")
    public ResponseEntity<DiagnosticTest> submitAnswer(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody AnswerSubmission submission) {
        UUID userId = extractUserId(userDetails);
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(diagnosticService.submitAnswer(userId, submission));
    }

    @GetMapping("/result")
    public ResponseEntity<TestResult> getResult(@AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = extractUserId(userDetails);
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(diagnosticService.getTestResult(userId));
    }
}