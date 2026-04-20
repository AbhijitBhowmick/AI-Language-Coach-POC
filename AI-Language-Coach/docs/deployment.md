# AI-Language-Coach: Deployment Guide

**Version:** 1.0  
**Date:** April 10, 2026

---

## 1. Overview

### 1.1 Environments

| Environment | Purpose | URL | Database |
|--------------|--------|-----|----------|
| Local | Development | http://localhost:8080 | H2/PostgreSQL |
| Dev | Development Testing | https://dev-api.language-coach.example.com | PostgreSQL |
| Staging | Pre-Production | https://staging-api.language-coach.example.com | PostgreSQL |
| Prod | Production | https://api.language-coach.example.com | PostgreSQL + Citus |

### 1.2 Architecture Diagram

```
┌──────────────────────────────────────────────────────────────────────┐
│                        ORACLE CLOUD AMPERE                         │
│  ┌─────────────────────────────────────────────────────────────────┐ │
│  │                    SPRING BOOT APP (ARM64)                      │ │
│  │                   12GB RAM, 4 OCPUs                            │ │
│  └─────────────────────────────────────────────────────────────────┘ │
│                               │                                      │
│     ┌─────────────────────────┼─────────────────────────┐            │
│     │                         │                         │            │
│  ┌──┴──┐                 ┌───┴───┐               ┌───┴───┐     │
│  │ App │                 │Valkey │               │Qdrant │     │
│  │ Pod │                 │7.2   │               │      │     │
│  └─────┘                 └──────┘               └──────┘     │
│                               │                         │            │
└───────────────────────────────┼─────────────────────────┼────────────┘
                              │                         │
                        PostgreSQL 16           PostgreSQL 16
                        (Primary DB)            (Vector DB)
```

---

## 2. Prerequisites

### 2.1 System Requirements

| Component | Minimum | Recommended |
|-----------|---------|-------------|
| OS | Oracle Linux 9 / Ubuntu 22.04 | Oracle Linux 9 |
| CPU | 2 cores Ampere | 4 cores Ampere |
| RAM | 8GB | 12GB |
| Storage | 50GB | 100GB |
| Podman | 4.x | 5.x |

### 2.2 Required Accounts

- [ ] Oracle Cloud Account (Free Tier)
- [ ] GitHub Repository
- [ ] Google Cloud Console (for Gemini API)
- [ ] Optional: Domain DNS setup

### 2.3 Environment Variables

Create `.env` file:

```bash
# Database
DB_PASSWORD=your_secure_password_here

# JWT
JWT_SECRET=your-256-bit-secret-key-minimum-32-characters

# Google AI (Gemini)
GEMINI_API_KEY=your_google_gemini_api_key

# OAuth2 (Google)
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret

# Application
SPRING_PROFILES_ACTIVE=prod
APP_HOST=0.0.0.0
APP_PORT=8080
```

---

## 3. Local Deployment

### 3.1 Quick Start

```bash
# 1. Clone repository
git clone https://github.com/your-org/AI-Language-Coach.git
cd AI-Language-Coach

# 2. Start infrastructure
podman-compose -f podman/container-compose.yaml up -d

# 3. Build application
./mvnw clean package -DskipTests

# 4. Run application
java -jar target/ai-language-coach-1.0.0.jar
```

### 3.2 Manual Setup

```bash
# Step 1: Start PostgreSQL
podman run -d --name coach-postgres \
  -p 5432:5432 \
  -e POSTGRES_DB=language_coach \
  -e POSTGRES_USER=coach_user \
  -e POSTGRES_PASSWORD=changeme \
  postgres:16-alpine

# Step 2: Start Valkey
podman run -d --name coach-valkey \
  -p 6379:6379 \
  valkey/valkey:7.2

# Step 3: Initialize database
podman exec -it coach-postgres psql -U coach_user -d language_coach \
  -f /path/to/src/main/resources/db/init-data.sql

# Step 4: Build and run
./mvnw spring-boot:run
```

### 3.3 Verify Local Deployment

```bash
# Check health
curl http://localhost:8080/api/v1/actuator/health

# Expected: {"status":"UP"}

# Check config
curl http://localhost:8080/api/v1/config/languages

# Expected: [{"languageCode":"cs",...}, ...]
```

---

## 4. Development Environment

### 4.1 Oracle Cloud Setup

```bash
# 1. Create Ampere instance (Always Free)
# Compute → Instances → Create → Ampere → VM.Standard.A1

# 2. Reserve public IP
# Networking → Reserved IP → Associate

# 3. Open ports
# Networking → Security Lists → Ingress Rules
# - 22 (SSH)
# - 80 (HTTP)
# - 443 (HTTPS)
# - 8080 (App)
```

### 4.2 Deploy on OCI

```bash
# SSH to instance
ssh opc@<your-public-ip>

# Install Podman
sudo dnf install -y podman
sudo systemctl enable --now podman

# Clone and setup
git clone https://github.com/your-org/AI-Language-Coach.git
cd AI-Language-Coach

# Create environment file
cat > .env << EOF
DB_PASSWORD=your_secure_password
JWT_SECRET=your-256-bit-secret-key-for-jwt-signing-minimum-32-characters
GEMINI_API_KEY=your_gemini_key
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret
SPRING_PROFILES_ACTIVE=dev
EOF

# Run infrastructure
podman-compose -f podman/container-compose.yaml up -d

# Build application
./mvnw clean package -DskipTests

# Run as systemd service
sudo podman generate systemd --name ai-language-coach \
  | sudo tee /etc/systemd/system/ai-language-coach.service
  
sudo systemctl daemon-reload
sudo systemctl enable --now ai-language-coach
```

### 4.3 Dev Environment Configuration

```bash
# application-dev.yml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/language_coach
    username: coach_user
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: validate
  data:
    redis:
      host: localhost
      port: 6379

server:
  port: 8080

logging:
  level:
    com.coach: DEBUG
```

---

## 5. Staging Environment

### 5.1 Staging Setup

```bash
# Create staging namespace
kubectl create namespace language-coach-staging

# Deploy with staging profile
./mvnw clean package -DskipTests

podman build -t language-coach-staging:latest .
podman tag language-coach-staging:latest registry.example.com/language-coach-staging:v1.0.0
podman push registry.example.com/language-coach-staging:v1.0.0
```

### 5.2 Staging Configuration

```yaml
# application-staging.yml
spring:
  datasource:
    url: ${DB_HOST}:5432/${DB_NAME}
    username: ${DB_USER}
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: validate
  data:
    redis:
      host: ${REDIS_HOST}
      port: 6379

server:
  port: 8080
  ssl:
    enabled: true

jwt:
  secret: ${JWT_SECRET}

langchain4j:
  google-ai-gemini:
    api-key: ${GEMINI_API_KEY}
```

---

## 6. Production Environment

### 6.1 Production Checklist

- [ ] Set production database password
- [ ] Configure SSL/TLS certificates
- [ ] Set up monitoring and alerts
- [ ] Configure backup strategy
- [ ] Test failover procedures
- [ ] Update DNS records

### 6.2 Production Deployment

```bash
# 1. Build production image
./mvnw clean package -Pprod

# 2. Build container
podman build -f Dockerfile.prod -t language-coach-prod:latest .

# 3. Run with production config
podman run -d \
  --name language-coach-prod \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_PASSWORD=${PROD_DB_PASSWORD} \
  -e JWT_SECRET=${PROD_JWT_SECRET} \
  -e GEMINI_API_KEY=${PROD_GEMINI_KEY} \
  --restart=always \
  language-coach-prod:latest
```

### 6.3 High Availability Setup

```yaml
# docker-compose.prod.yml
services:
  app:
    image: language-coach-prod:latest
    deploy:
      replicas: 3
      resources:
        limits:
          cpus: '2'
          memory: 4G
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - DB_HOST=postgres-cluster
      - REDIS_HOST=valkey-cluster
    depends_on:
      - postgres
      - valkey
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/api/v1/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3

  postgres:
    image: postgres:16-alpine
    volumes:
      - pgdata:/var/lib/postgresql/data
    environment:
      POSTGRES_USER: coach_user
      POSTGRES_PASSWORD: ${DB_PASSWORD}
      POSTGRES_DB: language_coach
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U coach_user"]
      interval: 10s
      timeout: 5s
      
  valkey:
    image: valkey/valkey:7.2
    command: valkey-server --appendonly yes
    volumes:
      - valkeydata:/data

volumes:
  pgdata:
  valkeydata:
```

---

## 7. Database Setup

### 7.1 Schema Initialization

```bash
# Create database
psql -U postgres -c "CREATE DATABASE language_coach;"

# Run schema
psql -U coach_user -d language_coach -f src/main/resources/db/init-data.sql

# Verify data
psql -U coach_user -d language_coach -c "SELECT * FROM system_config;"
psql -U coach_user -d language_coach -c "SELECT language_code, level FROM languages;"
```

### 7.2 Database Backup

```bash
# Backup command
pg_dump -U coach_user language_coach > backup_$(date +%Y%m%d).sql

# Restore command
psql -U coach_user language_coach < backup_20260410.sql
```

---

## 8. Monitoring & Troubleshooting

### 8.1 Health Checks

```bash
# Application health
curl http://localhost:8080/api/v1/actuator/health

# Database connection
curl http://localhost:8080/api/v1/actuator/health/db

# Redis connection
curl http://localhost:8080/api/v1/actuator/health/redis
```

### 8.2 Logs

```bash
# View logs
podman logs -f language-coach

# Error logs only
podman logs language-coach 2>&1 | grep -i error

# Last 100 lines
podman logs --tail 100 language-coach
```

### 8.3 Performance Monitoring

```bash
# CPU usage
podman stats --no-stream

# Memory usage
podman top language-coach

# JVM metrics
curl http://localhost:8080/api/v1/actuator/metrics/jvm.memory.used
```

---

## 9. Environment .env Files

### 9.1 Local (.env.local)
```bash
DB_PASSWORD=local_dev_pass
JWT_SECRET=dev-secret-key-minimum-32-characters
GEMINI_API_KEY=dev_api_key
GOOGLE_CLIENT_ID=dev_client
GOOGLE_CLIENT_SECRET=dev_secret
SPRING_PROFILES_ACTIVE=local
```

### 9.2 Development (.env.dev)
```bash
DB_PASSWORD=<complex_dev_password>
JWT_SECRET=<complex_dev_key>
GEMINI_API_KEY=<dev_gemini>
GOOGLE_CLIENT_ID=<dev_google>
GOOGLE_CLIENT_SECRET=<dev_google_secret>
SPRING_PROFILES_ACTIVE=dev
```

### 9.3 Production (.env.prod)
```bash
DB_PASSWORD=<very_complex_prod_password>
JWT_SECRET=<very_complex_prod_key>
GEMINI_API_KEY=<prod_gemini>
GOOGLE_CLIENT_ID=<prod_google>
GOOGLE_CLIENT_SECRET=<prod_google_secret>
SPRING_PROFILES_ACTIVE=prod
```

---

## 10. Quick Reference Commands

```bash
# Development
./mvnw spring-boot:run                              # Run locally
podman-compose -f podman/container-compose.yaml up  # Start infra

# Build
./mvnw clean package -DskipTests              # Build JAR
./mvnw spring-boot:build-image               # Build OCI image

# Deploy
podman-compose -f podman/container-compose.yaml up -d  # Start services
podman-compose -f podman/container-compose.yaml down    # Stop services

# Debug
podman exec -it <container> /bin/sh             # Shell into container
podman logs -f <container>                  # View logs

# Database
psql -U coach_user -d language_coach         # Connect to DB
```

---

## 11. Security Checklist

- [ ] Change default database password
- [ ] Change JWT secret in production
- [ ] Enable SSL/TLS in production
- [ ] Configure OAuth2 redirect URIs
- [ ] Set up firewall rules
- [ ] Enable audit logging
- [ ] Configure rate limiting
- [ ] Set up fail2ban (optional)

---

## 12. Troubleshooting Common Issues

| Issue | Solution |
|-------|---------|
| Redis connection refused | Check Valkey is running: `podman ps` |
| Database connection failed | Verify DB credentials in .env |
| OutOfMemoryError | Increase JVM heap: `-Xmx4g` |
| SSL handshake errors | Check valid certificates |
| 502 Bad Gateway | Check app health: `/api/v1/actuator/health` |