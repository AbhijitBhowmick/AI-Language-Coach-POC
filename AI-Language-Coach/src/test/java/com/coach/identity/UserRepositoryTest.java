package com.coach.identity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldFindUserByEmail() {
        User user = User.builder()
                .email("test@example.com")
                .password("hashedPassword")
                .firstName("John")
                .lastName("Doe")
                .provider("local")
                .tenantId("1")
                .build();

        entityManager.persist(user);
        entityManager.flush();

        Optional<User> found = userRepository.findByEmail("test@example.com");

        assertTrue(found.isPresent());
        assertEquals("test@example.com", found.get().getEmail());
    }

    @Test
    void shouldNotFindNonExistentUser() {
        Optional<User> found = userRepository.findByEmail("nonexistent@example.com");

        assertFalse(found.isPresent());
    }

    @Test
    void shouldCheckIfEmailExists() {
        User user = User.builder()
                .email("exists@example.com")
                .password("hashedPassword")
                .tenantId("1")
                .build();

        entityManager.persist(user);
        entityManager.flush();

        assertTrue(userRepository.existsByEmailAndTenantId("exists@example.com", "1"));
        assertFalse(userRepository.existsByEmailAndTenantId("nonexistent@example.com", "1"));
    }

    @Test
    void shouldSaveAndRetrieveUser() {
        User user = User.builder()
                .email("save@test.com")
                .password("password123")
                .firstName("Test")
                .lastName("User")
                .provider("google")
                .tenantId("1")
                .build();

        User saved = userRepository.save(user);
        entityManager.flush();
        entityManager.clear();

        assertNotNull(saved.getId());
        assertEquals("save@test.com", saved.getEmail());
    }
}