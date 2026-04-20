# AI-Language-Coach High-Level Design Architecture

## 1. System Overview

AI-Language-Coach is a **Universal Language Engine** - a voice-first multi-language learning platform that supports any target language, level, and native language through configuration (no hardcoding).

### Core Design Principles
- **Zero Hardcoding**: All languages, levels, prompts stored in database
- **Database-Driven**: Configurable via `system_config`, `languages`, `native_languages` tables
- **Multi-Language**: Add new languages by inserting database rows (no code changes)
- **Linguistic Bridging**: Grammar explanations tailored to user's native language

### Target Infrastructure
- **Platform**: Oracle Cloud Ampere (ARM64)
- **Memory**: 24GB RAM
- **Container Runtime**: Podman (Rootless/Daemonless)
- **Target Users**: 10 to 1,000,000 concurrent users

## 2. Configuration-Driven Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                 SYSTEM CONFIG                      │
├─────────────────────────────────────────────────────┤
│  Table: system_config                             │
│  ├── default.target.language = Czech (configurable)│
│  ├── default.target.level = A1 (configurable)       │
│  ├── default.native.language = en (configurable) │
│  └── linguistic.bridge.prompt = ... (configurable)│
├─────────────────────────────────────────────────────┤
│  Table: languages                                 │
│  ├── cs|Czech|A1, A2, B1                         │
│  ├── de|German|A1, A2                           │
│  ├── nl|Dutch|A1, A2                            │
│  └── [ANY NEW LANGUAGE]                          │
├─────────────────────────────────────────────────────┤
│  Table: diagnostic_questions                    │
│  (All questions from database, keyed by lang/level)│
└─────────────────────────────────────────────────────┘
```

## 3. Technology Stack

### Backend Framework
- **Language**: Java 21 (Virtual Threads)
- **Framework**: Spring Boot 3.4
- **AI Orchestration**: LangChain4j

### Data Layer
- **Primary Database**: PostgreSQL 16 (with Citus for future sharding)
- **Cache/Session Store**: Valkey 7.2+ (Redis-compatible, distributed mode)
- **Vector Database**: Qdrant (ARM64 binary)

### AI/ML
- **Standard LLM**: Gemini 1.5 Flash (Fast/High-RPM)
- **Premium LLM**: Gemini 1.5 Pro (Deep reasoning/Linguistic Bridge)

## 4. Component Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     Load Balancer                          │
└─────────────────────────────────────────────────────────────┘
                              │
                    ┌─────────┴───────��─┐
                    │   API Gateway     │
                    │   (Stateless)     │
                    └─────────┬─────────┘
                              │
           ┌────────────────────┼────────────────────┐
           │                    │                    │
     ┌─────┴─────┐        ┌─────┴─────┐        ┌─────┴─────┐
     │ Instance 1│        │ Instance 2│        │ Instance N│
     │ (Podman)  │        │ (Podman)  │        │ (Podman)  │
     └─────┬─────┘        └─────┬─────┘        └─────┬─────┘
           │                    │                    │
           └────────────────────┼────────────────────┘
                                │
           ┌────────────────────┼────────────────────┐
           │                    │                    │
     ┌─────┴─────┐        ┌─────┴─────┐        ┌─────┴─────┐
     │  Valkey   │        │ PostgreSQL│        │  Qdrant   │
     │  Cluster  │        │  + Citus  │        │           │
     └───────────┘        └───────────┘        └───────────┘
```

## 5. Phase 1: Security & Authentication ✅ COMPLETE

### Architecture
- **Current**: Spring Security with local PostgreSQL
- **Future**: OAuth2/OIDC ready via Adapter Pattern
- **Multi-tenancy**: JWT claims (tenant_id)

### Components
| Component | Description |
|-----------|-------------|
| SecurityPrincipalService | Adapter interface (Phase 1/2) |
| LocalSecurityPrincipalService | Phase 1 implementation |
| CustomUserDetails | UserDetails wrapper with roles |
| Role enum | STUDENT, ADMIN |
| SecurityConfig | formLogin configuration |
| JwtTokenProvider | JWT generation/validation |

### Security Flow
```
User → /api/v1/auth/register → JWT Token
User → /login (form) → Session → JWT Token
```

### Database Schema (Phase 1)
```sql
CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255),
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

## 6. Phase 2: RAG Integration (Future)

### Architecture
- **Vector Store**: Qdrant for semantic search
- **Content Types**: Grammar rules, dialogues, linguistic bridges

### Components
| Component | Description |
|-----------|-------------|
| RAGService | Query/ingest RAG content |
| EmbeddingService | Text-to-vector conversion |
| QdrantConfig | Qdrant client configuration |

### Collections
| Collection | Metadata Fields |
|-------------|-----------------|
| czech_a1_grammar | level, topic |
| czech_a2_grammar | level, topic |
| linguistic_bridges | source_lang, target_lang |

### API Endpoints
```
GET  /api/v1/rag/search?query=...&lang=cs&level=A1
POST /api/v1/rag/ingest (admin only)
```

## 7. Phase 3: Voice Conversation (Future)

### Architecture
- **STT**: Gemini 2.0 Flash (real-time)
- **TTS**: Gemini 2.0 Flash (audio output)
- **Voice Activity Detection**: MediaStream

### Components
| Component | Description |
|-----------|-------------|
| VoiceGateway | WebSocket handler |
| STTService | Speech-to-text |
| TTSService | Text-to-speech |
| ConversationManager | State machine for dialogue |

### API Endpoints
```
WS   /api/v1/voice/chat (WebSocket)
POST /api/v1/voice/stt (file upload)
GET  /api/v1/voice/tts?text=...
```

## 8. Phase 4: Adaptive Learning (Future)

### Architecture
- **Spaced Repetition**: SM-2 algorithm
- **Progress Tracking**: Per-skill mastery
- **Adaptive Difficulty**: ML-based content selection

### Components
| Component | Description |
|-----------|-------------|
| ProgressService | Track skill mastery |
| SchedulerService | Spaced repetition |
| RecommendationEngine | ML-based suggestions |

### Data Schema
```sql
CREATE TABLE user_skills (
    user_id UUID,
    skill VARCHAR(50),
    mastery_level FLOAT,
    next_review TIMESTAMP,
    PRIMARY KEY (user_id, skill)
);
```

## 9. Phase 5: Community Features (Future)

### Architecture
- **Peer Matching**: AI-powered practice partners
- **Social Features**: Leaderboards, challenges
- **Content Sharing**: User-generated exercises

### Components
| Component | Description |
|-----------|-------------|
| PeerMatchingService | Find practice partners |
| ChallengeService | Group challenges |
| CommunityContentService | User submissions |

## 10. Scalability Design

### Horizontal Scaling
- Stateless containerized services
- Podman pod deployment
- Auto-scaling based on CPU/memory metrics

### Valkey Sharding
- Cluster mode for distributed session data
- Key patterns:
  - `profile:{userId}` - User profiles
  - `diagnostic:test:{userId}` - Test state
  - `session:{sessionId}` - Active sessions

### API Endpoints

### Authentication
```
POST   /api/v1/auth/register     - Register new user
POST   /api/v1/auth/login        - Login with credentials
```

### Profile
```
POST   /api/v1/profile           - Create user profile
GET    /api/v1/profile           - Get user profile
PUT    /api/v1/profile           - Update profile
```

### Diagnostic Test
```
POST   /api/v1/diagnostic/start      - Start placement test
GET    /api/v1/diagnostic/question   - Get current question
POST   /api/v1/diagnostic/answer     - Submit answer
GET    /api/v1/diagnostic/result     - Get test results
```

## 11. Implementation Status

| Phase | Component | Status |
|-------|-----------|--------|
| 1 | SecurityPrincipalService | ✅ Complete |
| 1 | LocalSecurityPrincipalService | ✅ Complete |
| 1 | Role enum | ✅ Complete |
| 1 | CustomUserDetails | ✅ Complete |
| 1 | User entity (UUID + roles) | ✅ Complete |
| 1 | formLogin SecurityConfig | ✅ Complete |
| 1 | JWT Provider | ✅ Complete |
| 2 | RAG Integration | ⏳ Future |
| 3 | Voice Gateway | ⏳ Future |
| 4 | Adaptive Learning | ⏳ Future |
| 5 | Community Features | ⏳ Future |

## 12. Security Considerations

- JWT tokens with configurable expiration
- BCrypt password hashing
- CSRF protection (formLogin)
- Rate limiting (future)
- Input validation on all endpoints
- Secrets managed via environment variables