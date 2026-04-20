#!/bin/bash
#===============================================================================
# AI-Language-Coach: Native AOT Build Script
#
# Builds Spring Boot Native executable for 1M+ users
#
# Usage:
#   ./build-native.sh              # Build native executable
#   ./build-native.sh clean      # Clean and rebuild
#   ./build-native.sh podman     # Build using Podman
#===============================================================================

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

# Project settings
PROJECT_NAME="ai-language-platform"
MAIN_CLASS="com.coach.AiLanguageCoachApplication"

echo -e "${GREEN}============================================${NC}"
echo -e "${GREEN}  AI Language Platform - Native AOT Builder${NC}"
echo -e "${GREEN}============================================${NC}"

# -----------------------------------------------------------------------------
# Check prerequisites
# -----------------------------------------------------------------------------
check_java() {
    if ! command -v java &> /dev/null; then
        echo -e "${RED}Error: Java not found. Install JDK 21+${NC}"
        exit 1
    fi
    
    JAVA_VERSION=$(java -version 2>&1 | head -1 | cut -d'"' -f2 | cut -d'.' -f1)
    if [ "$JAVA_VERSION" -lt 21 ]; then
        echo -e "${RED}Error: Java 21+ required. Found: $JAVA_VERSION${NC}"
        exit 1
    fi
    
    echo -e "${GREEN}✓ Java 21+ detected${NC}"
}

check_maven() {
    if ! command -v ./mvnw &> /dev/null && ! command -v mvn &> /dev/null; then
        echo -e "${RED}Error: Maven not found${NC}"
        exit 1
    fi
    echo -e "${GREEN}✓ Maven detected${NC}"
}

# -----------------------------------------------------------------------------
# Check for GraalVM (optional but recommended)
# -----------------------------------------------------------------------------
check_graalvm() {
    if command -v gu &> /dev/null; then
        GU_VERSION=$(gu list 2>/dev/null | head -1 || echo "not installed")
        echo -e "${GREEN}✓ GraalVM detected: $GU_VERSION${NC}"
        
        # Install native-image if not present
        if ! gu list | grep -q "native-image"; then
            echo -e "${YELLOW}Installing native-image...${NC}"
            gu install native-image --version=21.0.0 || true
        fi
        
        NATIVE_AVAILABLE=true
    else
        echo -e "${YELLOW}⚠ GraalVM not found. Using Spring AOT fallback.${NC}"
        NATIVE_AVAILABLE=false
    fi
}

# -----------------------------------------------------------------------------
# Build native executable
# -----------------------------------------------------------------------------
build_native() {
    echo -e "${YELLOW}Building native executable...${NC}"
    
    # Use Spring Boot native support
    if [ "$NATIVE_AVAILABLE" = true ]; then
        echo -e "${GREEN}Using GraalVM Native Image...${NC}"
        ./mvnw -Pnative native:build -DskipTests
    else
        echo -e "${YELLOW}Using Spring AOT fallback...${NC}"
        ./mvnw -DskipTests package
    fi
    
    if [ -f "target/${PROJECT_NAME}" ]; then
        echo -e "${GREEN}✓ Native build successful!${NC}"
        ls -lh "target/${PROJECT_NAME}"
    else
        echo -e "${RED}✗ Build failed${NC}"
        exit 1
    fi
}

# -----------------------------------------------------------------------------
# Build with Podman/Docker
# -----------------------------------------------------------------------------
build_podman() {
    echo -e "${YELLOW}Building with Podman...${NC}"
    
    # Check for Podman/Docker
    if command -v podman &> /dev/null; then
        RUNTIME=podman
    elif command -v docker &> /dev/null; then
        RUNTIME=docker
    else
        echo -e "${RED}Error: Neither Podman nor Docker found${NC}"
        exit 1
    fi
    
    echo -e "${GREEN}Using: $RUNTIME${NC}"
    
    # Check for buildkit
    if $RUNTIME buildx version &> /dev/null 2>&1; then
        BUILDKIT="--load"
    else
        BUILDKIT=""
    fi
    
    # Try to build
    echo -e "${YELLOW}Building native image (this may take 5-10 minutes)...${NC}"
    
    # First, build regular JAR
    echo -e "${YELLOW}Building JAR...${NC}"
    ./mvnw -DskipTests package
    
    if [ -f "Dockerfile.native" ]; then
        $RUNTIME build -f Dockerfile.native $BUILDKIT -t ${PROJECT_NAME}:latest .
    else
        echo -e "${RED}Dockerfile.native not found${NC}"
        exit 1
    fi
    
    echo -e "${GREEN}✓ Podman build complete!${NC}"
}

# -----------------------------------------------------------------------------
# Clean build
# -----------------------------------------------------------------------------
clean_build() {
    echo -e "${YELLOW}Cleaning previous builds...${NC}"
    ./mvnw clean || true
    rm -f "target/${PROJECT_NAME}" || true
    echo -e "${GREEN}✓ Clean complete${NC}"
}

# -----------------------------------------------------------------------------
# Main
# -----------------------------------------------------------------------------
case "${1:-build}" in
    build)
        check_java
        check_maven
        check_graalvm
        build_native
        ;;
    clean)
        clean_build
        ;;
    podman)
        build_podman
        ;;
    *)
        echo "Usage: $0 {build|clean|podman}"
        exit 1
        ;;
esac

echo -e "${GREEN}============================================${NC}"
echo -e "${GREEN}  Build Complete!${NC}"
echo -e "${GREEN}============================================${NC}"