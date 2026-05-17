package com.platform.auth.controller;

import com.platform.auth.dto.*;
import com.platform.auth.entity.User;
import com.platform.auth.security.TenantContext;
import com.platform.auth.service.AuthenticationService;
import com.platform.auth.service.UserManagementService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/auth")
@SecurityRequirement(name = "bearerAuth")
public class AuthController {

    private final AuthenticationService authenticationService;
    private final UserManagementService userManagementService;

    public AuthController(AuthenticationService authenticationService, 
                         UserManagementService userManagementService) {
        this.authenticationService = authenticationService;
        this.userManagementService = userManagementService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody RegisterRequest request) {
        AuthenticationResponse response = authenticationService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authenticationService.login(request));
    }

    @GetMapping("/oauth2/success")
    public ResponseEntity<AuthenticationResponse> oauth2Success(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails instanceof User customUser) {
            return ResponseEntity.ok(authenticationService.oauth2Success(customUser));
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/validate")
    public ResponseEntity<Boolean> validateToken() {
        return ResponseEntity.ok(true);
    }

    @PostMapping("/users")
    @PreAuthorize("@roleChecker.isAllAdmins()")
    public ResponseEntity<UserResponse> createUser(
            @RequestBody CreateUserRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        UUID creatorId = UUID.fromString(userDetails.getUsername());
        String tenantId = TenantContext.getTenantId();
        
        com.platform.auth.entity.User newUser = authenticationService.createUserByAdmin(request, creatorId, tenantId);
        return ResponseEntity.status(HttpStatus.CREATED).body(userManagementService.toUserResponse(newUser));
    }

    @GetMapping("/users")
    @PreAuthorize("@roleChecker.isAllAdmins()")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        String tenantId = TenantContext.getTenantId();
        return ResponseEntity.ok(userManagementService.getAllUsers(tenantId));
    }

    @GetMapping("/users/pending")
    @PreAuthorize("@roleChecker.isElevatedAdmins()")
    public ResponseEntity<List<UserResponse>> getPendingApprovals() {
        String tenantId = TenantContext.getTenantId();
        return ResponseEntity.ok(userManagementService.getPendingApprovals(tenantId));
    }

    @PostMapping("/users/{userId}/approve")
    @PreAuthorize("@roleChecker.isElevatedAdmins()")
    public ResponseEntity<UserResponse> approveUser(
            @PathVariable UUID userId,
            @RequestBody(required = false) ApproveUserRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        UUID approverId = UUID.fromString(userDetails.getUsername());
        String comment = request != null ? request.comment() : null;
        
        return ResponseEntity.ok(userManagementService.approveUser(userId, approverId, comment));
    }

    @PostMapping("/users/{userId}/reject")
    @PreAuthorize("@roleChecker.isElevatedAdmins()")
    public ResponseEntity<UserResponse> rejectUser(
            @PathVariable UUID userId,
            @RequestBody RejectUserRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        UUID rejecterId = UUID.fromString(userDetails.getUsername());
        
        return ResponseEntity.ok(userManagementService.rejectUser(userId, rejecterId, request.reason()));
    }

    @PostMapping("/users/{userId}/suspend")
    @PreAuthorize("@roleChecker.isElevatedAdmins()")
    public ResponseEntity<UserResponse> suspendUser(
            @PathVariable UUID userId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        UUID suspenderId = UUID.fromString(userDetails.getUsername());
        
        return ResponseEntity.ok(userManagementService.suspendUser(userId, suspenderId));
    }

    @PostMapping("/users/{userId}/activate")
    @PreAuthorize("@roleChecker.isElevatedAdmins()")
    public ResponseEntity<UserResponse> activateUser(
            @PathVariable UUID userId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        UUID activatorId = UUID.fromString(userDetails.getUsername());
        
        return ResponseEntity.ok(userManagementService.activateUser(userId, activatorId));
    }

    @GetMapping("/users/{userId}")
    @PreAuthorize("@roleChecker.isAllAdmins()")
    public ResponseEntity<UserResponse> getUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(userManagementService.getUserById(userId));
    }
}