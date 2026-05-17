package com.coach.identity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CustomUserDetailsTest {

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .password("hashedPassword")
                .firstName("Test")
                .lastName("User")
                .provider("local")
                .tenantId("1")
                .roles(Set.of(Role.STUDENT))
                .enabled(true)
                .build();
    }

    @Test
    void shouldCreateCustomUserDetailsFromUser() {
        CustomUserDetails userDetails = new CustomUserDetails(testUser);

        assertEquals(testUser.getId(), userDetails.getId());
        assertEquals(testUser.getEmail(), userDetails.getEmail());
        assertEquals(testUser.getUsername(), userDetails.getUsername());
    }

    @Test
    void shouldReturnCorrectUsername() {
        CustomUserDetails userDetails = new CustomUserDetails(testUser);

        assertEquals("test@example.com", userDetails.getUsername());
    }

    @Test
    void shouldReturnCorrectPassword() {
        CustomUserDetails userDetails = new CustomUserDetails(testUser);

        assertEquals("hashedPassword", userDetails.getPassword());
    }

    @Test
    void shouldReturnCorrectEmail() {
        CustomUserDetails userDetails = new CustomUserDetails(testUser);

        assertEquals("test@example.com", userDetails.getEmail());
    }

    @Test
    void shouldReturnCorrectUserId() {
        CustomUserDetails userDetails = new CustomUserDetails(testUser);

        assertEquals(testUser.getId(), userDetails.getId());
    }

    @Test
    void shouldReturnAuthoritiesFromRoles() {
        CustomUserDetails userDetails = new CustomUserDetails(testUser);

        var authorities = userDetails.getAuthorities();
        assertEquals(1, authorities.size());
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT")));
    }

    @Test
    void shouldReturnEnabledTrue() {
        testUser.setEnabled(true);
        CustomUserDetails userDetails = new CustomUserDetails(testUser);

        assertTrue(userDetails.isEnabled());
    }

    @Test
    void shouldReturnEnabledFalse() {
        testUser.setEnabled(false);
        CustomUserDetails userDetails = new CustomUserDetails(testUser);

        assertFalse(userDetails.isEnabled());
    }

    @Test
    void shouldReturnAccountNonExpiredTrue() {
        testUser.setAccountNonExpired(true);
        CustomUserDetails userDetails = new CustomUserDetails(testUser);

        assertTrue(userDetails.isAccountNonExpired());
    }

    @Test
    void shouldReturnAccountNonLockedTrue() {
        testUser.setAccountNonLocked(true);
        CustomUserDetails userDetails = new CustomUserDetails(testUser);

        assertTrue(userDetails.isAccountNonLocked());
    }

    @Test
    void shouldReturnCredentialsNonExpiredTrue() {
        testUser.setCredentialsNonExpired(true);
        CustomUserDetails userDetails = new CustomUserDetails(testUser);

        assertTrue(userDetails.isCredentialsNonExpired());
    }

    @Test
    void shouldReturnDefaultRoleWhenRolesEmpty() {
        User userWithNoRoles = User.builder()
                .email("no-roles@example.com")
                .password("password")
                .tenantId("1")
                .roles(Set.of())
                .build();

        CustomUserDetails userDetails = new CustomUserDetails(userWithNoRoles);

        var authorities = userDetails.getAuthorities();
        assertEquals(1, authorities.size());
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }
}