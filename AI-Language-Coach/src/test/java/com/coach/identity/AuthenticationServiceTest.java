package com.coach.identity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        TenantContext.setTenantId("1");
        authenticationService = new AuthenticationService(
                userRepository, passwordEncoder, jwtTokenProvider);
    }

    @Test
    void shouldRegisterNewUser() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("newuser@example.com");
        request.setPassword("password123");
        request.setFirstName("John");
        request.setLastName("Doe");

        when(userRepository.existsByEmailAndTenantId(anyString(), anyString())).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");

        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            return User.builder()
                    .id(UUID.randomUUID())
                    .email(u.getEmail())
                    .password(u.getPassword())
                    .firstName(u.getFirstName())
                    .lastName(u.getLastName())
                    .provider(u.getProvider())
                    .tenantId(u.getTenantId())
                    .enabled(u.isEnabled())
                    .build();
        });

        when(jwtTokenProvider.generateToken(anyString(), anyString(), any())).thenReturn("jwt-token");

        AuthenticationResponse result = authenticationService.register(request);

        assertNotNull(result);
        assertEquals("newuser@example.com", result.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenEmailExists() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("existing@example.com");
        request.setPassword("password123");

        when(userRepository.existsByEmailAndTenantId(anyString(), anyString())).thenReturn(true);

        assertThrows(RuntimeException.class, () -> authenticationService.register(request));
    }

    @Test
    void shouldRegisterUserWithAdminRole() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("admin@example.com");
        request.setPassword("password123");
        request.setRole("ADMIN");

        when(userRepository.existsByEmailAndTenantId(anyString(), anyString())).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");

        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            return User.builder()
                    .id(UUID.randomUUID())
                    .email(u.getEmail())
                    .password(u.getPassword())
                    .roles(u.getRoles())
                    .build();
        });

        when(jwtTokenProvider.generateToken(anyString(), anyString(), any())).thenReturn("jwt-token");

        AuthenticationResponse result = authenticationService.register(request);

        assertNotNull(result);
    }

    @Test
    void shouldGenerateOAuth2Token() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("oauth@example.com")
                .password("oauth-password")
                .firstName("OAuth")
                .lastName("User")
                .provider("google")
                .tenantId("1")
                .build();

        when(jwtTokenProvider.generateToken(anyString(), anyString(), any())).thenReturn("oauth-token");

        AuthenticationResponse response = authenticationService.oauth2Success(user);

        assertNotNull(response);
        assertEquals("oauth-token", response.getAccessToken());
    }

    @Test
    void shouldFindOrCreateOAuthUserWhenNotExists() {
        when(userRepository.findByEmailAndTenantId(anyString(), anyString())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtTokenProvider.generateToken(anyString(), anyString(), any())).thenReturn("token");

        User result = authenticationService.findOrCreateOAuthUser("newoauth@example.com", "1", "OAuth", "User", "google");

        assertNotNull(result);
        assertEquals("newoauth@example.com", result.getEmail());
        assertEquals("google", result.getProvider());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldFindExistingOAuthUser() {
        User existingUser = User.builder()
                .id(UUID.randomUUID())
                .email("existing@example.com")
                .tenantId("1")
                .build();

        when(userRepository.findByEmailAndTenantId(anyString(), anyString())).thenReturn(Optional.of(existingUser));

        User result = authenticationService.findOrCreateOAuthUser("existing@example.com", "1", "Name", "Name", "google");

        assertEquals(existingUser, result);
        verify(userRepository, never()).save(any(User.class));
    }
}