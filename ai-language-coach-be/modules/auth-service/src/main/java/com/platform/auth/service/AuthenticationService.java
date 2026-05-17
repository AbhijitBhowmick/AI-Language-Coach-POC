package com.platform.auth.service;

import com.platform.auth.dto.*;
import com.platform.auth.entity.ApprovalRequest;
import com.platform.auth.entity.Role;
import com.platform.auth.entity.User;
import com.platform.auth.entity.User.UserStatus;
import com.platform.auth.repository.ApprovalRequestRepository;
import com.platform.auth.repository.UserRepository;
import com.platform.auth.security.JwtTokenProvider;
import com.platform.auth.security.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

@Service
public class AuthenticationService {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationService.class);

    private final UserRepository userRepository;
    private final ApprovalRequestRepository approvalRequestRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Value("${app.default-tenant-id:SYSTEM}")
    private String defaultTenantId;

    public AuthenticationService(UserRepository userRepository, 
                                ApprovalRequestRepository approvalRequestRepository,
                                PasswordEncoder passwordEncoder,
                                JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.approvalRequestRepository = approvalRequestRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional
    public AuthenticationResponse register(RegisterRequest request) {
        String tenantId = TenantContext.getTenantId() != null ? TenantContext.getTenantId() : defaultTenantId;
        
        Role requestedRole = Role.fromString(request.role());
        
        if (userRepository.existsByEmailAndTenantId(request.email(), tenantId)) {
            throw new com.platform.common.exception.AILanguageBusinessException("Email already registered in this tenant", "ERR_AUTH_003", 409);
        }

        boolean requiresApproval = requestedRole.requiresApproval();
        UserStatus initialStatus = requiresApproval ? UserStatus.PENDING_APPROVAL : UserStatus.ACTIVE;

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .firstName(request.firstName())
                .lastName(request.lastName())
                .provider("local")
                .tenantId(tenantId)
                .role(requestedRole)
                .status(initialStatus)
                .businessName(request.businessName())
                .enabled(true)
                .build();

        user = userRepository.save(user);
        log.info("User registered: {} with role {} and status {}", user.getEmail(), requestedRole, initialStatus);

        String token = jwtTokenProvider.generateToken(user.getEmail(), user.getId().toString(), tenantId);
        
        if (requiresApproval) {
            ApprovalRequest approvalRequest = new ApprovalRequest(
                user.getId(), 
                requestedRole.name(), 
                user.getId(),
                tenantId
            );
            approvalRequestRepository.save(approvalRequest);
            log.info("Approval request created for: {}", user.getEmail());
        }

        return AuthenticationResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtExpiration)
                .userId(user.getId().toString())
                .email(user.getEmail())
                .tenantId(tenantId)
                .status(user.getStatus().name())
                .roles(user.getRoles())
                .message(requiresApproval ? "Registration pending approval" : "Registration successful")
                .build();
    }

    @Transactional
    public User createUserByAdmin(CreateUserRequest request, UUID createdByUserId, String approverTenantId) {
        User creator = userRepository.findById(createdByUserId)
                .orElseThrow(() -> new RuntimeException("Creator user not found"));
        
        Role targetRole = Role.fromString(request.getRole());
        Role creatorRole = creator.getRoles() != null && !creator.getRoles().isEmpty() 
            ? creator.getRoles().iterator().next() 
            : Role.USER_STUDENT;
        
        if (!creatorRole.canCreateRole(targetRole)) {
            throw new com.platform.common.exception.AILanguageBusinessException("User " + creatorRole + " cannot create " + targetRole + " role", "ERR_AUTH_004", 403);
        }

        String tenantId = targetRole == Role.BUSINESS_ADMIN 
            ? UUID.randomUUID().toString() 
            : approverTenantId;

        boolean requiresApproval = targetRole.requiresApproval();
        UserStatus initialStatus = requiresApproval ? UserStatus.PENDING_APPROVAL : UserStatus.ACTIVE;

        User newUser = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .provider("admin-created")
                .tenantId(tenantId)
                .role(targetRole)
                .status(initialStatus)
                .businessName(request.getBusinessName())
                .createdBy(createdByUserId)
                .enabled(true)
                .build();

        newUser = userRepository.save(newUser);
        log.info("User created by admin: {} with role {}", newUser.getEmail(), targetRole);

        if (requiresApproval) {
            ApprovalRequest approvalRequest = new ApprovalRequest(
                newUser.getId(),
                targetRole.name(),
                createdByUserId,
                tenantId
            );
            approvalRequestRepository.save(approvalRequest);
        }

        return newUser;
    }

    @Transactional(readOnly = true)
    public AuthenticationResponse login(LoginRequest request) {
        String tenantId = TenantContext.getTenantId() != null ? TenantContext.getTenantId() : defaultTenantId;

        User user = userRepository.findByEmailAndTenantId(request.getEmail(), tenantId)
                .orElseThrow(() -> new com.platform.common.exception.AILanguageBusinessException("Invalid credentials", "ERR_AUTH_001", 401));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new com.platform.common.exception.AILanguageBusinessException("Invalid credentials", "ERR_AUTH_001", 401);
        }

        if (!user.canAccess()) {
            throw new com.platform.common.exception.AILanguageBusinessException("Account pending approval. Please wait for administrator to approve your registration.", "ERR_AUTH_002", 401);
        }

        String token = jwtTokenProvider.generateToken(user.getEmail(), user.getId().toString(), tenantId);

        return AuthenticationResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtExpiration)
                .userId(user.getId().toString())
                .email(user.getEmail())
                .tenantId(tenantId)
                .status(user.getStatus().name())
                .roles(user.getRoles())
                .build();
    }

    public AuthenticationResponse oauth2Success(User user) {
        String tenantId = user.getTenantId() != null ? user.getTenantId() : defaultTenantId;
        String token = jwtTokenProvider.generateToken(user.getEmail(), user.getId().toString(), tenantId);

        return AuthenticationResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtExpiration)
                .userId(user.getId().toString())
                .email(user.getEmail())
                .tenantId(tenantId)
                .status(user.getStatus().name())
                .build();
    }

    public User findOrCreateOAuthUser(String email, String tenantId, String firstName, String lastName, String provider) {
        return userRepository.findByEmailAndTenantId(email, tenantId)
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .email(email)
                            .password(null)
                            .firstName(firstName)
                            .lastName(lastName)
                            .provider(provider)
                            .tenantId(tenantId)
                            .role(Role.USER_STUDENT)
                            .status(UserStatus.ACTIVE)
                            .enabled(true)
                            .build();
                    return userRepository.save(newUser);
                });
    }
}