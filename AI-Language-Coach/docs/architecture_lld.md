# AI-Language-Coach: Low-Level Design (LLD)

**Version:** 2.0  
**Date:** April 10, 2026

## 0. Configuration-First Design

All defaults are database-driven via `ConfigService`:

```java
@Service
public class ConfigService {
    // Reads from system_config table with Valkey caching
    defaultTargetLanguage = getConfigValue("default.target.language", "Czech");
    defaultTargetLevel = getConfigValue("default.target.level", "A1");
    defaultNativeLanguage = getConfigValue("default.native.language", "en");
    linguisticBridgePrompt = getConfigValue("linguistic.bridge.prompt", "...");
}
```

## 1. Component Specifications

### 1.1 Identity Service (com.coach.identity)

| Component | Description |
|-----------|-----------|
| SecurityConfig | OAuth2 + JWT configuration |
| JwtTokenProvider | JWT generation/validation (256-bit secret) |
| JwtAuthenticationFilter | Request filtering |
| AuthenticationController | `/auth/register`, `/auth/login`, `/auth/oauth2/success` |
| User | JPA entity for PostgreSQL |

### 1.2 Profile Service (com.coach.profile)

| Component | Description |
|-----------|-----------|
| UserProfile | Record with nativeLanguage, planType, proficiencyScore |
| ProfileService | Valkey-based profile CRUD with 365-day TTL |
| PlanType | Enum (FREE, STANDARD, PREMIUM) |
| Valkey Keys | `profile:{userId}` |

### 1.3 Diagnostic Service (com.coach.diagnostic)

| Component | Description |
|-----------|-----------|
| QuestionBank | A1/A2 questions from curriculum |
| DiagnosticService | Test state management in Valkey |
| Valkey Keys | `diagnostic:test:{userId}` (TTL: 2 hours) |

### 1.4 Question Types

| Type | Description |
|------|-----------|
| GRAMMAR_COMPLETION | Fill in the blank with correct grammar form |
| VISUAL_MULTIPLE_CHOICE | Situational multiple choice with images |
| DIALOGUE_COMPLETION | Complete the conversation |
| LISTENING_FILL_BLANK | Audio-based fill in the blank |

## 2. RAG Architecture (Qdrant)

### Collections

| Collection | Metadata Fields | Purpose |
|------------|--------------|---------|
| czech_a1_grammar | level, topic | A1 rules |
| czech_a2_grammar | level, topic | A2 rules |
| exam_samples | level, source | Practice content |
| linguistic_bridges | source_lang, topic | Cross-linguistic notes |

## 3. Supported Native Languages

| Code | Language | Bridge Complexity |
|------|---------|--------------|
| bn | Bengali | High (Indo-Aryan → Slavic) |
| hi | Hindi | High (Indo-Aryan → Slavic) |
| te | Telugu | Very High (Dravidian → Slavic) |
| uk | Ukrainian | Low (Slavic → Slavic) |
| en | English | Medium (Germanic → Slavic) |

## 4. Database Schema

### PostgreSQL - Users Table
```sql
CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    provider VARCHAR(50),
    enabled BOOLEAN DEFAULT true,
    account_non_expired BOOLEAN DEFAULT true,
    account_non_locked BOOLEAN DEFAULT true,
    credentials_non_expired BOOLEAN DEFAULT true,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

### PostgreSQL - User Profiles Table
```sql
CREATE TABLE user_profiles (
    id UUID PRIMARY KEY,
    user_id UUID REFERENCES users(id),
    native_language VARCHAR(10),
    plan_type VARCHAR(20),
    proficiency_score DOUBLE,
    current_level VARCHAR(20),
    diagnostic_completed BOOLEAN DEFAULT false,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

### Valkey Keys Pattern

```
profile:{userId}           → UserProfile JSON (TTL: 365 days)
diagnostic:test:{userId}    → DiagnosticTest JSON (TTL: 2 hours)
session:{sessionId}        → Session data
```

## 5. API Endpoints

### Phase 1 - Authentication
```
POST   /api/v1/auth/register
POST   /api/v1/auth/login
GET    /api/v1/auth/oauth2/success
```

### Phase 1 - Profile
```
POST   /api/v1/profile
GET    /api/v1/profile
PUT    /api/v1/profile
```

### Phase 1 - Diagnostic
```
POST   /api/v1/diagnostic/start
GET    /api/v1/diagnostic/question
POST   /api/v1/diagnostic/answer
GET    /api/v1/diagnostic/result
```

### Phase 2 - RAG (Future)
```
GET    /api/v1/rag/search
POST   /api/v1/rag/ingest
```

### Phase 3 - Voice (Future)
```
WS     /api/v1/voice/chat
POST   /api/v1/voice/stt
GET    /api/v1/voice/tts
```

## 6. Configuration

### application.yml Structure
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/language_coach
    username: coach_user
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: update
  data:
    redis:
      host: localhost
      port: 6379

server:
  port: 8080
  servlet:
    context-path: /api/v1

jwt:
  secret: ${JWT_SECRET}
  expiration: 86400000

langchain4j:
  google-ai-gemini:
    api-key: ${GEMINI_API_KEY}
```

## 7. Cost Control Logic

| Plan | Requests/Min | Voice Minutes | LLM Model |
|------|-------------|-------------|-----------|
| FREE | 10 | 60 (Local) | Flash (Free Tier) |
| STANDARD | 60 | 500 | Flash (Paid) |
| PREMIUM | 120 | Unlimited | Pro (Paid) |

## 8. Implementation Status

| Component | Status | Priority |
|----------|--------|----------|
| SecurityConfig (Spring Security + formLogin) | Complete | High |
| SecurityPrincipalService (Adapter) | Complete | High |
| JWT | Complete | High |
| User JPA (UUID + roles) | Complete | High |
| ProfileService | Complete | High |
| ValkeyConfig | Complete | High |
| DiagnosticService | Complete | High |
| QuestionBank | Partial (10/50+) | Low |
| RAG Integration | Not Started | High |
| Voice Gateway | Not Started | Medium |
| Cost Tracking | Not Started | Medium |

## 9. Authentication - Phase 1 to Phase 2 Migration

### Phase 1: Spring Security + Local PostgreSQL (Current) ✅

| Component | Description | File |
|-----------|------------|-----|
| SecurityPrincipalService | Adapter interface | identity/SecurityPrincipalService.java |
| LocalSecurityPrincipalService | Phase 1 implementation | identity/LocalSecurityPrincipalService.java |
| User entity | UUID + roles (STUDENT/ADMIN) | identity/User.java |
| CustomUserDetails | UserDetails wrapper | identity/CustomUserDetails.java |
| Role enum | STUDENT, ADMIN | identity/Role.java |
| formLogin | Email/password login | SecurityConfig.java |
| BCrypt | Native password hashing | via PasswordEncoder |

### Phase 2: Keycloak Migration Path

The adapter pattern allows seamless migration from Phase 1 → Phase 2 without breaking changes:

```
┌─────────────────────────────────────────────────────┐
│         Application Code (Unchanged)               │
│   - Uses SecurityPrincipalService interface      │
└─────────────────────────────────────────────────────┘
                    │
                    │ (switch implementation)
                    ▼
┌─────────────────────────────────────────────────────┐
│  Phase 1: LocalSecurityPrincipalService          │
│  Phase 2: KeycloakSecurityPrincipalService       │
└─────────────────────────────────────────────��───────┘
```

#### Migration Steps (Phase 1 → Phase 2)

1. **Update application.yml** - Uncomment oauth2 section:
```yaml
security:
  oauth2:
    client:
      registration:
        keycloak:
          client-id: ${KEYCLOAK_CLIENT_ID:}
          client-secret: ${KEYCLOAK_CLIENT_SECRET:}
          scope: openid,email,profile
          authorization-grant-type: authorization_code
          redirect-uri: "{baseUrl}/login/oauth2/code/keycloak"
      provider:
        keycloak:
          issuer-uri: ${KEYCLOAK_ISSUER_URI:http://localhost:8080/realms/language-coach}
    resourceserver:
      jwt:
        issuer-uri: ${KEYCLOAK_ISSUER_URI:http://localhost:8080/realms/language-coach}
```

2. **Implement KeycloakSecurityPrincipalService**:
```java
@Service
@Primary
public class KeycloakSecurityPrincipalService implements SecurityPrincipalService {
    @Override
    public UUID getCurrentUserId() {
        // Extract from Keycloak JWT token
    }

    @Override
    public String getUsername() {
        // Extract from Keycloak security context
    }

    // ... other methods
}
```

3. **Delete or disable LocalSecurityPrincipalService**:
```java
// @Service  // Comment out or delete
// @Primary // Remove @Primary annotation
public class LocalSecurityPrincipalService implements SecurityPrincipalService {
    // Backup implementation
}
```

#### Keycloak Table Schema (Phase 2)

```sql
CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255),  -- null for Keycloak users
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    tenant_id VARCHAR(50) NOT NULL,
    enabled BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_roles (
    user_id UUID REFERENCES users(id),
    role VARCHAR(20),
    PRIMARY KEY (user_id, role)
);
```

#### Phase 2 OAuth Flow

```
User → /oauth2/authorization/keycloak → Keycloak Login Page →
Callback (/login/oauth2/code/keycloak) → Exchange code for tokens →
Extract user info from JWT → Create/update user in database →
Generate application JWT → Redirect to application
```

### Test Coverage

| Component | Unit Tests | Status |
|-----------|-----------|--------|
| SecurityPrincipalServiceTest | 12 tests | ✅ Passing |
| RoleTest | 4 tests | ✅ Passing |
| CustomUserDetailsTest | 14 tests | ✅ Passing |
| UserTest | 15 tests | ✅ Passing |
| AuthenticationServiceTest | 7 tests | ✅ Passing |