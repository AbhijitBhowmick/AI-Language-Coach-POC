package com.coach.identity;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Value("${platform.tenant.id:1}")
    private String defaultTenantId;

    public AuthenticationService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                                JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public AuthenticationResponse register(RegisterRequest request) {
        String tenantId = TenantContext.getTenantId() != null ? TenantContext.getTenantId() : defaultTenantId;
        String requestedRole = request.getRole() != null ? request.getRole().toUpperCase() : "STUDENT";

        if (userRepository.existsByEmailAndTenantId(request.getEmail(), tenantId)) {
            throw new RuntimeException("Email already registered in this tenant");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .provider("local")
                .tenantId(tenantId)
                .role(requestedRole.equals("ADMIN") ? Role.ADMIN : Role.STUDENT)
                .enabled(true)
                .build();

        user = userRepository.save(user);

        String token = jwtTokenProvider.generateToken(user.getEmail(), user.getId().toString(), tenantId);

        return AuthenticationResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtExpiration)
                .userId(user.getId().toString())
                .email(user.getEmail())
                .tenantId(tenantId)
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
                            .role(Role.STUDENT)
                            .enabled(true)
                            .build();
                    return userRepository.save(newUser);
                });
    }
}