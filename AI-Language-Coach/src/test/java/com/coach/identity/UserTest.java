package com.coach.identity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .password("hashedPassword")
                .firstName("John")
                .lastName("Doe")
                .provider("local")
                .tenantId("tenant-1")
                .roles(Set.of(Role.STUDENT))
                .enabled(true)
                .build();
    }

    @Test
    void shouldCreateUserWithAllFields() {
        assertNotNull(user.getId());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("hashedPassword", user.getPassword());
        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertEquals("local", user.getProvider());
        assertEquals("tenant-1", user.getTenantId());
        assertTrue(user.isEnabled());
    }

    @Test
    void shouldReturnEmailAsUsername() {
        assertEquals("test@example.com", user.getUsername());
    }

    @Test
    void shouldReturnRolesAsAuthorities() {
        var authorities = user.getAuthorities();

        assertEquals(1, authorities.size());
        assertTrue(authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT")));
    }

    @Test
    void shouldReturnDefaultRoleUserWhenRolesNull() {
        user.setRoles(null);
        var authorities = user.getAuthorities();

        assertEquals(1, authorities.size());
        assertTrue(authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void shouldReturnDefaultRoleUserWhenRolesEmpty() {
        user.setRoles(Set.of());
        var authorities = user.getAuthorities();

        assertEquals(1, authorities.size());
        assertTrue(authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void shouldReturnMultipleRolesAsAuthorities() {
        user.setRoles(Set.of(Role.STUDENT, Role.ADMIN));
        var authorities = user.getAuthorities();

        assertEquals(2, authorities.size());
    }

    @Test
    void shouldReturnTrueForIsEnabled() {
        assertTrue(user.isEnabled());
    }

    @Test
    void shouldReturnTrueForIsAccountNonExpired() {
        assertTrue(user.isAccountNonExpired());
    }

    @Test
    void shouldReturnTrueForIsAccountNonLocked() {
        assertTrue(user.isAccountNonLocked());
    }

    @Test
    void shouldReturnTrueForCredentialsNonExpired() {
        assertTrue(user.isCredentialsNonExpired());
    }

    @Test
    void shouldReturnFalseForIsEnabledWhenDisabled() {
        user.setEnabled(false);
        assertFalse(user.isEnabled());
    }

    @Test
    void shouldBuildUserWithAdminRole() {
        User adminUser = User.builder()
                .email("admin@example.com")
                .password("password")
                .tenantId("tenant-1")
                .role(Role.ADMIN)
                .build();

        assertTrue(adminUser.getRoles().contains(Role.ADMIN));
    }

    @Test
    void shouldDefaultToStudentRole() {
        User defaultUser = User.builder()
                .email("default@example.com")
                .password("password")
                .tenantId("tenant-1")
                .build();

        assertTrue(defaultUser.getRoles().contains(Role.STUDENT));
    }
}