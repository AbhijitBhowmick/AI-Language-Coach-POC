# Graph Report - .  (2026-04-19)

## Corpus Check
- Corpus is ~21,609 words - fits in a single context window. You may not need a graph.

## Summary
- 580 nodes · 785 edges · 32 communities detected
- Extraction: 73% EXTRACTED · 27% INFERRED · 0% AMBIGUOUS · INFERRED: 214 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- [[_COMMUNITY_Diagnostic Test|Diagnostic Test]]
- [[_COMMUNITY_Diagnostic Test|Diagnostic Test]]
- [[_COMMUNITY_Authentication|Authentication]]
- [[_COMMUNITY_Diagnostic Test|Diagnostic Test]]
- [[_COMMUNITY_Identity & Tenant|Identity & Tenant]]
- [[_COMMUNITY_Diagnostic Test|Diagnostic Test]]
- [[_COMMUNITY_User Profile|User Profile]]
- [[_COMMUNITY_Language Config|Language Config]]
- [[_COMMUNITY_Language Config|Language Config]]
- [[_COMMUNITY_Language Config|Language Config]]
- [[_COMMUNITY_Language Config|Language Config]]
- [[_COMMUNITY_Language Config|Language Config]]
- [[_COMMUNITY_Language Config|Language Config]]
- [[_COMMUNITY_Authentication|Authentication]]
- [[_COMMUNITY_Diagnostic Test|Diagnostic Test]]
- [[_COMMUNITY_Diagnostic Test|Diagnostic Test]]
- [[_COMMUNITY_Language Config|Language Config]]
- [[_COMMUNITY_Authentication|Authentication]]
- [[_COMMUNITY_Identity & Tenant|Identity & Tenant]]
- [[_COMMUNITY_Authentication|Authentication]]
- [[_COMMUNITY_Language Config|Language Config]]
- [[_COMMUNITY_Authentication|Authentication]]
- [[_COMMUNITY_Language Config|Language Config]]
- [[_COMMUNITY_Diagnostic Test|Diagnostic Test]]
- [[_COMMUNITY_Diagnostic Test|Diagnostic Test]]
- [[_COMMUNITY_Diagnostic Test|Diagnostic Test]]
- [[_COMMUNITY_Diagnostic Test|Diagnostic Test]]
- [[_COMMUNITY_Diagnostic Test|Diagnostic Test]]
- [[_COMMUNITY_Diagnostic Test|Diagnostic Test]]
- [[_COMMUNITY_Diagnostic Test|Diagnostic Test]]
- [[_COMMUNITY_Diagnostic Test|Diagnostic Test]]
- [[_COMMUNITY_User Profile|User Profile]]

## God Nodes (most connected - your core abstractions)
1. `QuestionEntity` - 41 edges
2. `User` - 33 edges
3. `DiagnosticQuestion` - 32 edges
4. `DiagnosticTest` - 23 edges
5. `UserProfile` - 22 edges
6. `PlanConfig` - 21 edges
7. `LanguageConfig` - 21 edges
8. `SystemConfig` - 19 edges
9. `TestResult` - 17 edges
10. `NativeLanguage` - 17 edges

## Surprising Connections (you probably didn't know these)
- None detected - all connections are within the same source files.

## Communities

### Community 0 - "Diagnostic Test"
Cohesion: 0.04
Nodes (6): AnswerSubmission, DiagnosticService, DiagnosticTest, QuestionFeedback, QuestionFeedbackBuilder, TestResultBuilder

### Community 1 - "Diagnostic Test"
Cohesion: 0.04
Nodes (2): DiagnosticQuestionBuilder, QuestionEntity

### Community 2 - "Authentication"
Cohesion: 0.07
Nodes (7): AuthenticationController, AuthenticationResponseBuilder, AuthenticationService, LangChainConfig, TenantAwareUserDetailsService, UserBuilder, UserProfileBuilder

### Community 3 - "Diagnostic Test"
Cohesion: 0.06
Nodes (5): DiagnosticController, ProfileService, ProfileUpdateRequest, PromptTemplateService, UserProfile

### Community 4 - "Identity & Tenant"
Cohesion: 0.06
Nodes (1): User

### Community 5 - "Diagnostic Test"
Cohesion: 0.06
Nodes (1): DiagnosticQuestion

### Community 6 - "User Profile"
Cohesion: 0.09
Nodes (5): JwtAuthenticationFilter, ProfileController, TenantAuthenticationFilter, TenantContext, TenantResolver

### Community 7 - "Language Config"
Cohesion: 0.07
Nodes (4): ConfigController, LanguageConfigRepository, QuestionBank, QuestionEntityRepository

### Community 8 - "Language Config"
Cohesion: 0.1
Nodes (5): ConfigService, defaultContext(), LearningContext(), NativeLanguageRepository, SystemConfigRepository

### Community 9 - "Language Config"
Cohesion: 0.09
Nodes (1): PlanConfig

### Community 10 - "Language Config"
Cohesion: 0.1
Nodes (1): LanguageConfig

### Community 11 - "Language Config"
Cohesion: 0.1
Nodes (1): SystemConfig

### Community 12 - "Language Config"
Cohesion: 0.11
Nodes (1): NativeLanguage

### Community 13 - "Authentication"
Cohesion: 0.12
Nodes (1): AuthenticationResponse

### Community 14 - "Diagnostic Test"
Cohesion: 0.13
Nodes (1): TestResult

### Community 15 - "Diagnostic Test"
Cohesion: 0.16
Nodes (1): DiagnosticTestBuilder

### Community 16 - "Language Config"
Cohesion: 0.15
Nodes (1): RegisterRequest

### Community 17 - "Authentication"
Cohesion: 0.28
Nodes (1): SecurityConfig

### Community 18 - "Identity & Tenant"
Cohesion: 0.22
Nodes (2): CustomUserDetailsService, UserRepository

### Community 19 - "Authentication"
Cohesion: 0.33
Nodes (1): AuthenticationRequest

### Community 20 - "Language Config"
Cohesion: 0.67
Nodes (1): AiLanguageCoachApplication

### Community 21 - "Authentication"
Cohesion: 0.67
Nodes (1): JwtAuthenticationEntryPoint

### Community 22 - "Language Config"
Cohesion: 0.67
Nodes (1): ValkeyConfig

### Community 23 - "Diagnostic Test"
Cohesion: 1.0
Nodes (0): 

### Community 24 - "Diagnostic Test"
Cohesion: 1.0
Nodes (0): 

### Community 25 - "Diagnostic Test"
Cohesion: 1.0
Nodes (0): 

### Community 26 - "Diagnostic Test"
Cohesion: 1.0
Nodes (0): 

### Community 27 - "Diagnostic Test"
Cohesion: 1.0
Nodes (0): 

### Community 28 - "Diagnostic Test"
Cohesion: 1.0
Nodes (0): 

### Community 29 - "Diagnostic Test"
Cohesion: 1.0
Nodes (0): 

### Community 30 - "Diagnostic Test"
Cohesion: 1.0
Nodes (0): 

### Community 31 - "User Profile"
Cohesion: 1.0
Nodes (0): 

## Knowledge Gaps
- **Thin community `Diagnostic Test`** (1 nodes): `UserRepositoryTest.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Diagnostic Test`** (1 nodes): `AuthenticationServiceTest.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Diagnostic Test`** (1 nodes): `TestConfig.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Diagnostic Test`** (1 nodes): `QuestionBankTest.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Diagnostic Test`** (1 nodes): `DiagnosticControllerIntegrationTest.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Diagnostic Test`** (1 nodes): `DiagnosticServiceTest.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Diagnostic Test`** (1 nodes): `ProfileControllerIntegrationTest.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Diagnostic Test`** (1 nodes): `ProfileServiceTest.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `User Profile`** (1 nodes): `PlanType.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.