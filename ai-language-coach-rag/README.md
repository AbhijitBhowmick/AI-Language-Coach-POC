# AI Language Coach - Phase 2 Monorepo

Multi-service repository containing Phase 2 components for the AI Language Coach platform.

## Services

| Service | Path | Framework | Port |
|---------|------|-----------|------|
| RAG Engine | `ai-universal-rag-engine/` | FastAPI (Python 3.12) | 8000 |
| Frontend | `ai-language-coach-frontend/` | Next.js 15 (React 19) | 3000 |

## Quick Start

### Local Development

```bash
# Start all services
docker-compose up -d

# Start specific service
docker-compose up ai-universal-rag-engine
docker-compose up ai-language-coach-frontend

# View logs
docker-compose logs -f
```

### Environment Variables

Create `.env` file:

```env
# RAG Engine
GEMINI_API_KEY=your-gemini-api-key
USE_MOCK=false
QDRANT_HOST=qdrant
QDRANT_PORT=6333
VALKEY_HOST=valkey
VALKEY_PORT=6379

# Frontend
JAVA_API_URL=http://localhost:8080/api/v1
RAG_API_URL=http://localhost:8000
```

## CI/CD Pipeline

### Workflows

| Workflow | Trigger | Description |
|----------|---------|-------------|
| `phase2-rag.yml` | Push to `ai-universal-rag-engine/` | Python RAG Engine CI/CD |
| `phase2-frontend.yml` | Push to `ai-language-coach-frontend/` | Next.js Frontend CI/CD |
| `phase2-all.yml` | Push to main/develop | Both services together |

### Pipeline Stages

1. **Lint & Test** - Code quality checks
2. **Security Scan** - Bandit, pip-audit, npm audit, Gitleaks
3. **Build** - Compile/build code
4. **Push** - Push Podman image to GHCR
5. **Deploy** - SSH to local server (commented)

### GitHub Secrets (for deployment)

| Secret | Description |
|--------|-------------|
| `LOCAL_SERVER_IP` | SSH target IP |
| `LOCAL_SERVER_USER` | SSH username |
| `SSH_PRIVATE_KEY` | SSH key |

## Infrastructure

The `docker-compose.yaml` includes:

- **Qdrant** - Vector database (port 6333)
- **Valkey** - Cache (port 6379)
- **RAG Engine** - FastAPI backend (port 8000)
- **Frontend** - Next.js UI (port 3000)

## Project Structure

```
ai-language-coach-phase2/
├── ai-universal-rag-engine/     # Python FastAPI RAG Engine
│   ├── src/
│   ├── tests/
│   ├── Dockerfile
│   └── pyproject.toml
├── ai-language-coach-frontend/  # Next.js 15 Frontend
│   ├── src/
│   ├── Dockerfile
│   └── package.json
├── docker-compose.yaml           # Unified compose
├── .github/workflows/
│   ├── phase2-rag.yml
│   ├── phase2-frontend.yml
│   └── phase2-all.yml
└── README.md
```

## Tech Stack

### RAG Engine
- Python 3.12
- FastAPI
- Qdrant (vector store)
- Valkey (cache)
- Google Generative AI

### Frontend
- Next.js 15
- React 19
- TypeScript
- Tailwind CSS
- TanStack Query
- Recharts

## License

MIT
