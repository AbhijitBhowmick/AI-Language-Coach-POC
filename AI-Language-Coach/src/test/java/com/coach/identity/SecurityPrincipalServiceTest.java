package com.coach.identity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityPrincipalServiceTest {

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private CustomUserDetails customUserDetails;

    private LocalSecurityPrincipalService principalService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        principalService = new LocalSecurityPrincipalService();
    }

    @Test
    void shouldReturnUserIdFromCustomUserDetails() {
        UUID userId = UUID.randomUUID();
        when(authentication.getPrincipal()).thenReturn(customUserDetails);
        when(customUserDetails.getId()).thenReturn(userId);

        UUID result = principalService.getCurrentUserId();

        assertEquals(userId, result);
    }

    @Test
    void shouldReturnNullWhenPrincipalIsNotCustomUserDetails() {
        when(authentication.getPrincipal()).thenReturn("not-a-custom-user");

        UUID result = principalService.getCurrentUserId();

        assertNull(result);
    }

    @Test
    void shouldReturnUsernameFromUserDetails() {
        when(authentication.getPrincipal()).thenReturn(customUserDetails);
        when(customUserDetails.getUsername()).thenReturn("testuser");

        String result = principalService.getUsername();

        assertEquals("testuser", result);
    }

    @Test
    void shouldReturnNullWhenPrincipalIsNotUserDetails() {
        when(authentication.getPrincipal()).thenReturn("anonymous");

        String result = principalService.getUsername();

        assertNull(result);
    }

    @Test
    void shouldReturnEmailFromCustomUserDetails() {
        when(authentication.getPrincipal()).thenReturn(customUserDetails);
        when(customUserDetails.getEmail()).thenReturn("test@example.com");

        String result = principalService.getEmail();

        assertEquals("test@example.com", result);
    }

    @Test
    void shouldReturnNullWhenPrincipalIsNotCustomUserDetailsForEmail() {
        when(authentication.getPrincipal()).thenReturn(mock(UserDetails.class));

        String result = principalService.getEmail();

        assertNull(result);
    }

    @Test
    void shouldReturnTrueWhenUserHasRole() {
        GrantedAuthority authority = new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_STUDENT");
        Collection<GrantedAuthority> authorities = List.of(authority);
        doReturn(authorities).when(authentication).getAuthorities();

        boolean result = principalService.hasRole("STUDENT");

        assertTrue(result);
    }

    @Test
    void shouldReturnFalseWhenUserDoesNotHaveRole() {
        GrantedAuthority authority = new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN");
        Collection<GrantedAuthority> authorities = List.of(authority);
        doReturn(authorities).when(authentication).getAuthorities();

        boolean result = principalService.hasRole("STUDENT");

        assertFalse(result);
    }

    @Test
    void shouldReturnTrueForIsStudent() {
        GrantedAuthority authority = new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_STUDENT");
        Collection<GrantedAuthority> authorities = List.of(authority);
        doReturn(authorities).when(authentication).getAuthorities();

        boolean result = principalService.isStudent();

        assertTrue(result);
    }

    @Test
    void shouldReturnFalseForIsStudentWhenNotStudent() {
        GrantedAuthority authority = new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN");
        Collection<GrantedAuthority> authorities = List.of(authority);
        doReturn(authorities).when(authentication).getAuthorities();

        boolean result = principalService.isStudent();

        assertFalse(result);
    }

    @Test
    void shouldReturnTrueForIsAdmin() {
        GrantedAuthority authority = new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN");
        Collection<GrantedAuthority> authorities = List.of(authority);
        doReturn(authorities).when(authentication).getAuthorities();

        boolean result = principalService.isAdmin();

        assertTrue(result);
    }

    @Test
    void shouldReturnFalseForIsAdminWhenNotAdmin() {
        GrantedAuthority authority = new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_STUDENT");
        Collection<GrantedAuthority> authorities = List.of(authority);
        doReturn(authorities).when(authentication).getAuthorities();

        boolean result = principalService.isAdmin();

        assertFalse(result);
    }

    @Test
    void shouldHandleCaseInsensitiveRoleCheck() {
        GrantedAuthority authority = new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_STUDENT");
        Collection<GrantedAuthority> authorities = List.of(authority);
        doReturn(authorities).when(authentication).getAuthorities();

        boolean result = principalService.hasRole("student");

        assertTrue(result);
    }
}