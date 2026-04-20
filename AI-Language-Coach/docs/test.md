# AI-Language-Coach Phase 1: Test Documentation

**Version:** 1.0  
**Date:** April 10, 2026

---

## 1. Test Overview

### 1.1 Test Categories

| Category | Type | Coverage |
|----------|------|----------|
| Unit Tests | JUnit 5 + Mockito | Core services |
| Integration Tests | SpringBootTest | REST APIs |
| Component Tests | TestContainers | Database |

### 1.2 Test Location
```
src/test/java/com/coach/
├── identity/
│   ├── UserRepositoryTest.java
│   └── AuthenticationServiceTest.java
├── profile/
│   ├── ProfileServiceTest.java
│   └── ProfileControllerIntegrationTest.java
├── diagnostic/
│   ├── QuestionBankTest.java
│   ├── DiagnosticServiceTest.java
│   └── DiagnosticControllerIntegrationTest.java
└── common/
    └── config/
        └── ConfigServiceTest.java
```

---

## 2. Unit Test Scenarios

### 2.1 Identity Service Tests

#### Test: UserRepositoryTest
```java
@Test void shouldFindUserByEmail()
@Test void shouldNotFindNonExistentUser()
@Test void shouldCheckIfEmailExists()
@Test void shouldSaveAndRetrieveUser()
```

**Coverage:**
- ✅ JPA repository CRUD operations
- ✅ Email uniqueness validation
- ✅ User entity mapping

#### Test: AuthenticationServiceTest
```java
@Test void shouldRegisterNewUser()
@Test void shouldThrowExceptionWhenEmailExists()
@Test void shouldAuthenticateWithValidCredentials()
@Test void shouldThrowExceptionForNonExistentUser()
@Test void shouldGenerateOAuth2Token()
```

**Coverage:**
- ✅ User registration flow
- ✅ Email duplicate checking
- ✅ JWT token generation
- ✅ OAuth2 success handling

---

### 2.2 Profile Service Tests

#### Test: ProfileServiceTest
```java
@Test void shouldCreateProfile()
@Test void shouldGetProfile()
@Test void shouldReturnNullWhenProfileNotFound()
@Test void shouldUpdateProfile()
@Test void shouldUpdateReadinessScore()
@Test void shouldDeleteProfile()
@Test void shouldThrowExceptionWhenUpdatingNonExistentProfile()
```

**Coverage:**
- ✅ Profile creation with LearningContext
- ✅ Valkey cache operations
- ✅ Profile updates
- ✅ Readiness score calculation

---

### 2.3 Diagnostic Service Tests

#### Test: DiagnosticServiceTest
```java
@Test void shouldStartDiagnosticTest()
@Test void shouldGetCurrentQuestion()
@Test void shouldReturnNullWhenTestNotFound()
@Test void shouldSubmitCorrectAnswer()
@Test void shouldSubmitIncorrectAnswer()
@Test void shouldCompleteTestAndUpdateProfile()
@Test void shouldGetTestResult()
@Test void shouldDetermineA2LevelForHighScore()
@Test void shouldDetermineA1LevelForMediumScore()
```

**Coverage:**
- ✅ Diagnostic test initialization
- ✅ Question retrieval
- ✅ Answer validation
- ✅ Score calculation
- ✅ Level determination logic

#### Test: QuestionBankTest
```java
@Test void shouldLoadQuestionsFromDatabase()
@Test void shouldReturnAvailableLanguages()
@Test void shouldReturnAvailableLevels()
@Test void shouldGetQuestionsByTargetLanguageAndLevel()
```

**Coverage:**
- ✅ Database question loading
- ✅ Multi-language support
- ✅ Level filtering

---

### 2.4 Configuration Tests

#### Test: ConfigServiceTest
```java
@Test void shouldLoadDefaultsFromDatabase()
@Test void shouldUseCache()
@Test void shouldReloadConfig()
@Test void shouldReturnAvailableLanguages()
```

**Coverage:**
- ✅ System config loading
- ✅ Valkey caching
- ✅ Config reload

---

## 3. Integration Test Scenarios

### 3.1 Profile Controller Integration

**Endpoint:** `POST /api/v1/profile`

```bash
# Create Profile
curl -X POST http://localhost:8080/api/v1/profile \
  -H "Authorization: Bearer <token>" \
  -d "targetLanguage=Czech&targetLevel=A1&nativeLanguage=en&planType=FREE"

# Expected: 200 OK
# Response: {...,"context":{"targetLanguage":"Czech","targetLevel":"A1","nativeLanguage":"en"},...}
```

**Test Scenarios:**

| Scenario | Input | Expected Status | Validation |
|----------|-------|-----------------|------------|
| Create valid profile | targetLanguage=Czech | 200 | Context matches |
| Create with defaults | (no params) | 200 | Uses config defaults |
| Invalid plan type | planType=INVALID | 400 | Validation error |
| Without auth | (no token) | 401 | Unauthorized |

---

### 3.2 Diagnostic Controller Integration

**Endpoint:** `POST /api/v1/diagnostic/start`

```bash
# Start Test with specific language/level
curl -X POST http://localhost:8080/api/v1/diagnostic/start \
  -H "Authorization: Bearer <token>" \
  -d "targetLanguage=German&targetLevel=A1"

# Expected: 200 OK
# Response: {"userId":"...","targetLanguage":"German","targetLevel":"A1",...}
```

| Scenario | Input | Expected Status | Validation |
|----------|-------|-----------------|------------|
| Start Czech A1 | targetLanguage=Czech | 200 | Language matches |
| Start German A1 | targetLanguage=German | 200 | Questions loaded |
| Start with no params | (none) | 200 | Uses config defaults |
| Without auth | (no token) | 401 | Unauthorized |

---

### 3.3 Configuration API Tests

**Endpoint:** `GET /api/v1/config/languages`

```bash
# Get available languages
curl http://localhost:8080/api/v1/config/languages

# Expected: 200 OK
# Response: [{"languageCode":"cs","languageName":"Czech",...}, ...]
```

**Endpoint:** `GET /api/v1/config/questions/languages`

```bash
# Get languages with questions
curl http://localhost:8080/api/v1/config/questions/languages

# Expected: 200 OK
# Response: ["Czech","German","Dutch"]
```

---

## 4. Test Data Requirements

### 4.1 Database Seed Data

Run `src/main/resources/db/init-data.sql` before tests:

```bash
# PostgreSQL
psql -U coach_user -d language_coach -f src/main/resources/db/init-data.sql

# Or Liquibase will auto-run on startup
```

### 4.2 Required Test Data

| Table | Required Records |
|-------|------------------|
| `system_config` | 6 records (defaults) |
| `languages` | 7+ records (Czech, German, Dutch levels) |
| `native_languages` | 5 records (en, bn, hi, te, uk) |
| `diagnostic_questions` | 10+ questions |
| `plan_types` | 3 records (FREE, STANDARD, PREMIUM) |

---

## 5. Running Tests

### 5.1 Local Testing

```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=ProfileServiceTest

# Run with coverage
./mvnw test jacoco:report
```

### 5.2 Test Configuration

```yaml
# src/test/resources/application-test.yml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
  data:
    redis:
      host: localhost
      port: 6379
```

### 5.3 External Dependencies

```bash
# Start Valkey for tests
podman run -d --name test-valkey -p 6379:6379 valkey/valkey:7.2

# Start PostgreSQL for tests  
podman run -d --name test-postgres -p 5432:5432 \
  -e POSTGRES_DB=language_coach \
  -e POSTGRES_USER=coach_user \
  -e POSTGRES_PASSWORD=changeme \
  postgres:16-alpine
```

---

## 6. Test Coverage Goals

| Component | Target Coverage |
|-----------|----------------|
| Identity Service | 80%+ |
| Profile Service | 85%+ |
| Diagnostic Service | 85%+ |
| Config Service | 70%+ |
| **Overall** | **80%+** |

---

## 7. Manual Test Scenarios

### 7.1 Configuration Test

| Step | Action | Expected Result |
|------|--------|----------------|
| 1 | Start application | Config loads from DB |
| 2 | Check logs | "Default configuration loaded: language=Czech, level=A1" |
| 3 | Query `/api/v1/config/languages` | Returns configured languages |
| 4 | Update `system_config` set value | Next restart uses new value |

### 7.2 Multi-Language Test

| Step | Action | Expected Result |
|------|--------|----------------|
| 1 | POST `/api/v1/profile?targetLanguage=German` | Profile with German context |
| 2 | POST `/api/v1/diagnostic/start?targetLanguage=German` | German questions loaded |
| 3 | Complete test | Score calculated |
| 4 | GET `/api/v1/profile` | German A2 if passed |

### 7.3 Linguistic Bridge Test

| Step | Action | Expected Result |
|------|--------|----------------|
| 1 | Set plan to PREMIUM | Premium features enabled |
| 2 | Answer question incorrectly | Explanation with bridge |
| 3 | Check explanation contains | Native language concept |

---

## 8. Troubleshooting

### 8.1 Common Errors

| Error | Cause | Solution |
|-------|-------|----------|
| `No questions found` | Questions not seeded | Run init-data.sql |
| `Config null` | ConfigService not initialized | Check application startup |
| `Redis connection refused` | Valkey not running | Start Valkey container |

### 8.2 Debug Commands

```bash
# Check Valkey
valkey-cli ping
# Should return: PONG

# Check PostgreSQL
psql -U coach_user -d language_coach -c "SELECT * FROM system_config;"

# Check logs
grep "Initializing system configuration" app.log
```

---

## 9. CI/CD Pipeline

```yaml
# .github/workflows/test.yml
name: Phase 1 Tests
on: [push, pull_request]
jobs:
  test:
    runs-on: ubuntu-latest
    services:
      postgres:
        image: postgres:16-alpine
      valkey:
        image: valkey/valkey:7.2
    steps:
      - uses: actions/checkout@v4
      - name: Run tests
        run: ./mvnw test
      - name: Upload coverage
        uses: codecov/codecov-action@v3
```