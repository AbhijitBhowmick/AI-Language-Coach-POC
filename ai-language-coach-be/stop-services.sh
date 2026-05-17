#!/bin/bash
# Stop script for all AI Language Coach microservices

echo "Stopping AI Language Coach services..."

pkill -f "auth-service" 2>/dev/null && echo "  auth-service stopped" || echo "  auth-service not running"
pkill -f "profile-service" 2>/dev/null && echo "  profile-service stopped" || echo "  profile-service not running"
pkill -f "diagnostic-service" 2>/dev/null && echo "  diagnostic-service stopped" || echo "  diagnostic-service not running"
pkill -f "voice-service" 2>/dev/null && echo "  voice-service stopped" || echo "  voice-service not running"

# Cleanup PID files
rm -f /tmp/auth-service.pid /tmp/profile-service.pid /tmp/diagnostic-service.pid /tmp/voice-service.pid

echo ""
echo "All services stopped."