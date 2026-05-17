#!/bin/bash
# Generate swagger.json for all microservices
# Usage: ./generate-swagger.sh [base_url]
# Example: ./generate-swagger.sh http://localhost

set -e

BASE_URL="${1:-http://localhost}"
OUTPUT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/swagger"

mkdir -p "$OUTPUT_DIR"

SERVICES=(
    "auth-service:8081"
    "profile-service:8082"
    "diagnostic-service:8083"
    "voice-service:8084"
    "community-service:8085"
)

echo "========================================"
echo "  Generating swagger.json files"
echo "========================================"
echo ""
echo "Output directory: $OUTPUT_DIR"
echo ""

for SERVICE in "${SERVICES[@]}"; do
    NAME="${SERVICE%%:*}"
    PORT="${SERVICE##*:}"
    URL="${BASE_URL}:${PORT}/v3/api-docs"
    OUTPUT="${OUTPUT_DIR}/${NAME}.json"

    echo "Fetching ${NAME} from ${URL}..."

    HTTP_CODE=$(curl -s -o "$OUTPUT" -w "%{http_code}" --connect-timeout 5 --max-time 10 "$URL" 2>/dev/null || echo "000")

    if [ "$HTTP_CODE" = "200" ]; then
        echo "  ✅ ${NAME} — saved to ${OUTPUT} (HTTP ${HTTP_CODE})"
    else
        echo "  ❌ ${NAME} — failed (HTTP ${HTTP_CODE})"
        rm -f "$OUTPUT"
    fi
done

echo ""
echo "========================================"
echo "  Generation complete!"
echo "========================================"
echo ""
echo "Files:"
ls -la "$OUTPUT_DIR"/*.json 2>/dev/null || echo "  No swagger.json files generated."

echo ""
echo "To import into Postman:"
echo "  Postman → Import → OpenAPI → Select swagger/*.json"
echo ""
echo "To use with Newman (E2E testing):"
echo "  npm install -D newman"
echo "  npx newman run swagger/auth-service.json"
echo ""