package com.coach.identity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoleTest {

    @Test
    void shouldReturnCorrectAuthorityForStudent() {
        assertEquals("ROLE_STUDENT", Role.STUDENT.getAuthority());
    }

    @Test
    void shouldReturnCorrectAuthorityForAdmin() {
        assertEquals("ROLE_ADMIN", Role.ADMIN.getAuthority());
    }

    @Test
    void shouldHaveTwoRoles() {
        assertEquals(2, Role.values().length);
    }

    @Test
    void shouldFindRoleByName() {
        assertEquals(Role.STUDENT, Role.valueOf("STUDENT"));
        assertEquals(Role.ADMIN, Role.valueOf("ADMIN"));
    }
}