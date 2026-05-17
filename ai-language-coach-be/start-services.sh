#!/bin/bash
# Start script for all AI Language Coach microservices
# Usage: ./start-services.sh [db_host] [valkey_host]
# Example: ./start-services.sh 192.168.0.18 192.168.0.18

set -e

# Default values
DB_HOST="${1:-localhost}"
DB_PORT="${2:-5432}"
VALKEY_HOST="${3:-localhost}"
VALKEY_PORT="${4:-6379}"
DB_NAME="language_coach"
DB_USER="infra_admin"
DB_PASS="Test@1234"

# Resolve script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$SCRIPT_DIR"

# JVM options for local network connectivity
JVM_OPTS="-Djdk.net.hosts.file=/etc/hosts -Djava.net.preferIPv4Stack=true --enable-native-access=ALL-UNNAMED"

# Connection timeout
CONN_TIMEOUT="--spring.datasource.hikari.connection-timeout=30000"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  AI Language Coach - Starting Services${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo "Configuration:"
echo "  Database:    ${DB_HOST}:${DB_PORT}/${DB_NAME}"
echo "  Valkey:      ${VALKEY_HOST}:${VALKEY_PORT}"
echo "  Auth:        port 8081"
echo "  Profile:     port 8082"
echo "  Diagnostic:  port 8083"
echo "  Voice:       port 8084"
echo ""

# Function to start a service
start_service() {
    local SERVICE_NAME=$1
    local JAR_PATH=$2
    local PORT=$3

    if [ ! -f "$JAR_PATH" ]; then
        echo -e "${RED}ERROR: JAR not found: $JAR_PATH${NC}"
        echo "         Run 'mvn clean package -DskipTests' first"
        return 1
    fi

    echo -e "${YELLOW}Starting ${SERVICE_NAME} on port ${PORT}...${NC}"
    nohup java $JVM_OPTS \
        -jar "$JAR_PATH" \
        --spring.datasource.url="jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}" \
        --spring.datasource.username="$DB_USER" \
        --spring.datasource.password="$DB_PASS" \
        --spring.data.redis.host="$VALKEY_HOST" \
        --spring.data.redis.port="$VALKEY_PORT" \
        --server.port="$PORT" \
        $CONN_TIMEOUT \
        > /tmp/${SERVICE_NAME}.log 2>&1 &

    echo $! > /tmp/${SERVICE_NAME}.pid
    echo "  PID: $(cat /tmp/${SERVICE_NAME}.pid)"
    echo "  Log: /tmp/${SERVICE_NAME}.log"
}

# Kill any existing services
echo "Stopping any existing services..."
pkill -f "auth-service" 2>/dev/null || true
pkill -f "profile-service" 2>/dev/null || true
pkill -f "diagnostic-service" 2>/dev/null || true
pkill -f "voice-service" 2>/dev/null || true
pkill -f "community-service" 2>/dev/null || true
sleep 2

# Start all services
start_service "auth-service" \
    "${PROJECT_ROOT}/modules/auth-service/target/auth-service-1.0.0-SNAPSHOT.jar" \
    "8081"

sleep 2

start_service "profile-service" \
    "${PROJECT_ROOT}/modules/profile-service/target/profile-service-1.0.0-SNAPSHOT.jar" \
    "8082"

sleep 2

start_service "diagnostic-service" \
    "${PROJECT_ROOT}/modules/diagnostic-service/target/diagnostic-service-1.0.0-SNAPSHOT.jar" \
    "8083"

sleep 2

start_service "voice-service" \
    "${PROJECT_ROOT}/modules/voice-service/target/voice-service-1.0.0-SNAPSHOT.jar" \
    "8084"

sleep 2

start_service "community-service" \
    "${PROJECT_ROOT}/modules/community-service/target/community-service-1.0.0-SNAPSHOT.jar" \
    "8085"

echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  All services starting...${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo "Wait ~10 seconds for services to initialize, then test:"
echo ""
echo "  curl -X POST http://localhost:8081/auth/register \\"
echo "    -H 'Content-Type: application/json' \\"
echo "    -d '{\"email\":\"test@test.com\",\"password\":\"Test1234\",\"firstName\":\"Test\",\"lastName\":\"User\"}'"
echo ""
echo "To check status:"
echo "  curl http://localhost:8081/auth/validate"
echo ""
echo "To stop all services:"
echo "  ./stop-services.sh"
echo ""
echo "To view logs:"
echo "  tail -f /tmp/auth-service.log"
echo "  tail -f /tmp/profile-service.log"
echo "  tail -f /tmp/diagnostic-service.log"
echo "  tail -f /tmp/voice-service.log"
echo ""