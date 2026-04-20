# Security Architecture: Phase 1 → Phase 2

**Date:** April 19, 2026  
**Status:** Implemented (Phase 1), Ready for Phase 2

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│              Spring Security + Local DB (Phase 1)               │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│              SecurityPrincipalService (Adapter)                │
│                     [Interface]                             │
└─────────────────────────────────────────────────────────────────┘
                                │
                    ┌───────────┴───────────┐
                    ▼                       ▼
┌──────────────────────────┐    ┌──────────────────────────┐
│ LocalSecurityPrincipal    │    │ KeycloakPrincipal      │
│   Service (Phase 1)      │    │ Service (Phase 2)     │
│   - Local PostgreSQL     │    │   - Keycloak OIDC      │
│   - BCrypt passwords    │    │   - JWT tokens         │
└──────────────────────────┘    └──────────────────────────┘
```

---

## Phase 1: Current Implementation

### Files Created

| File | Purpose |
|------|---------|
| `SecurityPrincipalService.java` | Adapter interface |
| `LocalSecurityPrincipalService.java` | Phase 1 impl |
| `Role.java` | STUDENT/ADMIN enum |
| `CustomUserDetails.java` | UserDetails wrapper |
| `SecurityConfig.java` | formLogin + oauth2 slot |

### Security Configuration

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/register").permitAll()
                .requestMatchers("/api/v1/coach/**").hasRole("STUDENT")
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/api/v1/coach/dashboard", true)
            )
            // Phase 2: Uncomment to enable Keycloak
            // .oauth2Login(oauth2 -> ...)
            .logout(logout -> logout.logoutSuccessUrl("/login?logout=true"));
        return http.build();
    }
}
```

### User Entity with Roles

```java
@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Set<Role> roles;
}
```

---

## Phase 2: Keycloak Migration

When ready to migrate to Keycloak:

### Step 1: Update application.yml

```yaml
security:
  oauth2:
    client:
      registration:
        keycloak:
          client-id: ${KEYCLOAK_CLIENT_ID:}
          client-secret: ${KEYCLOAK_CLIENT_SECRET:}
          issuer-uri: ${KEYCLOAK_ISSUER_URI:}
```

### Step 2: Create KeycloakSecurityPrincipalService

```java
@Service
@Primary  // Switch from LocalSecurityPrincipalService
public class KeycloakSecurityPrincipalService implements SecurityPrincipalService {
    
    @Override
    public UUID getCurrentUserId() {
        // Get from JWT token
    }
    
    @Override
    public boolean hasRole(String role) {
        // Check JWT claims
    }
}
```

### Step 3: Enable oauth2Login

In SecurityConfig, uncomment `.oauth2Login()` and remove formLogin.

---

## Why This Works

- **Zero code changes** - Business logic calls `SecurityPrincipalService`
- **Adapter pattern** - Swap implementations without touching business logic
- **UUID alignment** - User IDs match Keycloak format
- **Clean schema** - identity_vault ready for Keycloak import