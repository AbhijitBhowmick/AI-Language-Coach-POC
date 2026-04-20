# AI-Language-Coach: Setup Guide

**Version:** 1.0  
**Date:** April 10, 2026

---

## 1. Overview

This guide covers setting up the AI-Language-Coach Phase 1 application on local development machines (Mac/Ubuntu).

### 1.1 Components Required

```
┌─────────────────────────────────────────────────────────────────────────────┐
│              AI-LANGUAGE-COACH STACK                 │
├─────────────────────────────────────────────────────────────────┤
│  Java 21        → Application Runtime              │
│  Maven         → Build Tool                      │
│  PostgreSQL    → Primary Database              │
│  Valkey        → Cache/Session Store              │
│  Spring Boot   → Web Framework               │
└─────────────────────────────────────────┘
```

### 1.2 System Requirements

| Component | Mac (Apple Silicon/Intel) | Ubuntu |
|-----------|----------------------|--------|
| OS | macOS 12+ | Ubuntu 22.04+ |
| Java | JDK 21 (Temurin) | OpenJDK 21 |
| RAM | 8GB+ | 8GB+ |
| Disk | 20GB+ | 20GB+ |
| Docker/Podman | Docker Desktop 4.x | Podman 4.x |

---

## 2. Prerequisites Installation

### 2.1 macOS Setup

#### Step 1: Install Homebrew (if not installed)

```bash
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
```

#### Step 2: Install Java 21 (Temurin)

```bash
# Option A: Using Homebrew (Intel)
brew install openjdk@21

# Option B: Using Homebrew (Apple Silicon)
brew install openjdk@21

# Add to PATH (add to ~/.zshrc or ~/.bashrc)
echo 'export PATH="/usr/local/opt/openjdk@21/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc
```

Or download from: https://adoptium.net/temurin/releases/

#### Step 3: Install PostgreSQL

```bash
# Option A: Using Homebrew
brew install postgresql@16
brew services start postgresql@16

# Option B: Using Docker (Recommended)
docker run -d --name coach-postgres \
  -p 5432:5432 \
  -e POSTGRES_PASSWORD=coach \
  -e POSTGRES_DB=language_coach \
  postgres:16-alpine

# Verify
docker ps
```

#### Step 4: Install Valkey (or Redis)

```bash
# Option A: Using Homebrew
brew install valkey
brew services start valkey

# Option B: Using Docker (Recommended)
docker run -d --name coach-valkey \
  -p 6379:6379 \
  valkey/valkey:7.2

# Verify
docker ps
docker exec coach-valkey valkey-cli ping
```

#### Step 5: Install Maven

```bash
# Using Homebrew
brew install maven

# Verify
mvn -version
```

#### Step 6: Install Docker Desktop (if using Docker)

Download from: https://www.docker.com/products/docker-desktop/

---

### 2.2 Ubuntu Setup

#### Step 1: Update System

```bash
sudo apt update && sudo apt upgrade -y
```

#### Step 2: Install Java 21

```bash
# Install OpenJDK 21
sudo apt install -y openjdk-21-jdk

# Verify
java -version

# Set JAVA_HOME
echo 'export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64' >> ~/.bashrc
echo 'export PATH=$JAVA_HOME/bin:$PATH' >> ~/.bashrc
source ~/.bashrc
```

#### Step 3: Install PostgreSQL 16

```bash
# Add PostgreSQL repository
sudo sh -c 'echo "deb http://apt.postgresql.org/pub/repos/apt $(lsb_release -cs)-pgdg main" > /etc/apt/sources.list.d/pgdg.list'
curl -fsSL https://www.postgresql.org/media/keys/ACCC4CF8.asc | sudo gpg --dearmor -o /etc/apt/trusted.gpg.d/postgresql.gpg
sudo apt update

# Install PostgreSQL 16
sudo apt install -y postgresql-16

# Start PostgreSQL
sudo systemctl enable postgresql
sudo systemctl start postgresql

# Verify
sudo -u postgres psql --version
```

#### Step 4: Install Valkey

```bash
# Install Valkey
sudo apt install -y valkey-server

# Start Valkey
sudo systemctl enable valkey-server
sudo systemctl start valkey-server

# Verify
valkey-cli ping
```

#### Step 5: Install Maven

```bash
sudo apt install -y maven

# Verify
mvn -version
```

#### Step 6: Install Podman (Optional)

```bash
# Add Podman repository
. /etc/os-release
sudo sh -c "echo 'deb http://download.opensuse.org/repositories/devel:/kubic:/containers:/ stables/$UBUNTU_CODENAME/devel:kubic:containers.stables_$UBUNTU_CODENAME.beta2_all.deb' > /etc/apt/sources.list.d/devel:kubic:containers.list"
curl -fsSL https://download.opensuse.org/repositories/devel:/kubic:/containers:/Ubuntu_$(lsb_release -rs)/Release.key | gpg --dearmor | sudo tee /etc/apt/trusted.gpg.d/kubic.gpg > /dev/null
sudo apt update
sudo apt install -y podman

# Verify
podman --version
```

---

## 3. Database Setup

### 3.1 Create Database and User

```bash
# Connect to PostgreSQL
sudo -u postgres psql

# Create database and user
CREATE DATABASE language_coach;
CREATE USER coach_user WITH PASSWORD 'changeme';
GRANT ALL PRIVILEGES ON DATABASE language_coach TO coach_user;
\c language_coach
GRANT ALL ON SCHEMA public TO coach_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO coach_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO coach_user;

# Exit
\q
```

### 3.2 Initialize Schema

```bash
# Run initialization SQL
psql -U coach_user -d language_coach -f src/main/resources/db/init-data.sql

# Verify tables created
psql -U coach_user -d language_coach -c "\dt"

# Expected output:
#  languages
#  system_config
#  native_languages
#  diagnostic_questions
#  plan_types
#  users
```

### 3.3 Verify Configuration

```bash
# Check system config
psql -U coach_user -d language_coach -c "SELECT config_key, config_value FROM system_config;"

# Expected:
#  default.target.language | Czech
#  default.target.level | A1
#  default.native.language | en
#  linguistic.bridge.prompt | ...
```

---

## 4. Application Configuration

### 4.1 Create Environment File

Create `.env` file in project root:

```bash
# .env
DB_PASSWORD=changeme
JWT_SECRET=your-256-bit-secret-key-for-jwt-signing-minimum-32-characters
GEMINI_API_KEY=your_gemini_api_key
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret
SPRING_PROFILES_ACTIVE=local
```

### 4.2 Update application.yml

Edit `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/language_coach
    username: coach_user
    password: changeme
    driver-class-name: org.postgresql.Driver
  
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
  
  data:
    redis:
      host: localhost
      port: 6379

server:
  port: 8080
  servlet:
    context-path: /api/v1

jwt:
  secret: your-256-bit-secret-key-for-jwt-signing-minimum-32-characters
  expiration: 86400000

langchain4j:
  google-ai-gemini:
    api-key: your_gemini_api_key

logging:
  level:
    com.coach: DEBUG
```

---

## 5. Build and Run

### 5.1 Build Application

```bash
# Navigate to project directory
cd AI-Language-Coach

# Build (skipping tests for initial run)
./mvnw clean package -DskipTests

# Verify JAR created
ls -la target/*.jar
```

### 5.2 Run Application

```bash
# Run using Spring Boot
./mvnw spring-boot:run

# OR run JAR directly
java -jar target/ai-language-coach-1.0.0-SNAPSHOT.jar
```

### 5.3 Verify Application

```bash
# Check health endpoint
curl http://localhost:8080/api/v1/actuator/health

# Expected: {"status":"UP"}

# Check configured languages
curl http://localhost:8080/api/v1/config/languages

# Expected: [{"languageCode":"cs",...}, ...]
```

---

## 6. Using Docker/Podman (Alternative)

### 6.1 Start Infrastructure Only

```bash
# Using Docker Compose
docker-compose -f podman/container-compose.yaml up -d

# Verify containers running
docker ps
```

### 6.2 Full Stack with Podman

```bash
# podman/docker-compose.yml
version: '3.8'

services:
  postgres:
    image: postgres:16-alpine
    container_name: coach-postgres
    ports:
      - "5432:5432"
    environment:
      POSTGRES_DB: language_coach
      POSTGRES_USER: coach_user
      POSTGRES_PASSWORD: changeme
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U coach_user"]
      interval: 10s
      timeout: 5s

  valkey:
    image: valkey/valkey:7.2
    container_name: coach-valkey
    ports:
      - "6379:6379"
    volumes:
      - valkey_data:/data
    healthcheck:
      test: ["CMD", "valkey-cli", "ping"]
      interval: 10s

volumes:
  postgres_data:
  valkey_data:
```

### 6.3 Connect Application to Docker Services

Update `application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/language_coach
  data:
    redis:
      host: localhost
```

---

## 7. Verification Steps

### 7.1 Health Checks

| Component | Command | Expected |
|-----------|---------|----------|
| PostgreSQL | `psql -U coach_user -d language_coach -c "SELECT 1"` | `?column? = 1` |
| Valkey | `valkey-cli ping` | `PONG` |
| App | `curl http://localhost:8080/api/v1/actuator/health` | `{"status":"UP"}` |
| Config | `curl http://localhost:8080/api/v1/config/languages` | JSON array |

### 7.2 API Tests

```bash
# Test 1: Register user
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"test123","firstName":"Test","lastName":"User"}'

# Test 2: Login
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"test123"}'

# Test 3: Create profile
curl -X POST http://localhost:8080/api/v1/profile \
  -H "Authorization: Bearer <token>" \
  -d "targetLanguage=Czech&nativeLanguage=en"

# Test 4: Start diagnostic
curl -X POST http://localhost:8080/api/v1/diagnostic/start \
  -H "Authorization: Bearer <token>"
```

---

## 8. Troubleshooting

### 8.1 Common Issues

| Issue | Cause | Solution |
|-------|-------|----------|
| `port already in use` | PostgreSQL already running on 5432 | Stop existing or change port |
| `valkey-cli: command not found` | Valkey not in PATH | Use Docker: `docker exec coach-valkey valkey-cli ping` |
| `JDK 21 not found` | PATH not set | Add to ~/.bashrc: `export PATH=$JAVA_HOME/bin:$PATH` |
| `DB connection refused` | PostgreSQL not running | `brew services start postgresql@16` or `sudo systemctl start postgresql` |

### 8.2 Port Conflicts

```bash
# Check what's using the port
macOS: lsof -i :5432
Ubuntu: sudo netstat -tulpn | grep 5432

# Change port in application.yml if needed
spring:
  datasource:
    url: jdbc:postgresql://localhost:5433/language_coach
```

### 8.3 Permission Issues (Ubuntu)

```bash
# Fix PostgreSQL permission
sudo chown -R postgres:postgres /var/lib/postgresql
sudo chmod -R 700 /var/lib/postgresql

# Restart PostgreSQL
sudo systemctl restart postgresql
```

---

## 9. Quick Reference

### 9.1 Key Commands

| Action | macOS | Ubuntu |
|--------|------|--------|
| Start PostgreSQL | `brew services start postgresql@16` | `sudo systemctl start postgresql` |
| Start Valkey | `brew services start valkey` | `sudo systemctl start valkey` |
| Stop PostgreSQL | `brew services stop postgresql@16` | `sudo systemctl stop postgresql` |
| Stop Valkey | `brew services stop valkey` | `sudo systemctl stop valkey` |
| Connect to DB | `psql -U coach_user -d language_coach` | `sudo -u postgres psql` |

### 9.2 Directory Structure

```
AI-Language-Coach/
├── src/
│   ├── main/
│   │   ├── java/com/coach/
│   │   │   ├── identity/        # Auth service
│   │   │   ├── profile/       # Profile service
│   │   │   ├── diagnostic/   # Diagnostic service
│   │   │   └── common/     # Shared config
│   │   └── resources/
│   │       ├── application.yml
│   │       └── db/init-data.sql
│   └── test/
│       └── java/com/coach/
├── docs/
│   ├── test.md
│   ├── deployment.md
│   └── setup.md          # This file
├── podman/
│   └── container-compose.yaml
├── target/
│   └── ai-language-coach.jar
└── pom.xml
```

---

## 10. Next Steps

After setup:

1. ✅ Verify application starts
2. ✅ Test REST APIs manually
3. ✅ Review test documentation (`docs/test.md`)
4. ✅ Configure for development/production (`docs/deployment.md`)

---

## 11. Getting Help

- **Issues**: https://github.com/your-org/AI-Language-Coach/issues
- **Documentation**: See `docs/` folder
- **Logs**: Check console output or `logs/app.log`