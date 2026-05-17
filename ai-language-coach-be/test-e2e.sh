#!/bin/bash
# E2E Integration Tests for AI Language Coach
# Tests all 5 microservices end-to-end
# Usage: ./test-e2e.sh [base_url] [admin_email] [admin_password]

set -e

BASE_URL="${1:-http://localhost}"
ADMIN_EMAIL="${2:-admin@platform.com}"
ADMIN_PASS="${3:-ChangeMe123!}"
PASS=0
FAIL=0

green() { echo -e "\033[0;32m$1\033[0m"; }
red()   { echo -e "\033[0;31m$1\033[0m"; }
bold()  { echo -e "\033[1m$1\033[0m"; }

assert_eq() {
    local desc="$1" expected="$2" actual="$3"
    if [ "$expected" = "$actual" ]; then
        green "  ✅ $desc"
        PASS=$((PASS+1))
    else
        red "  ❌ $desc (expected: $expected, got: $actual)"
        FAIL=$((FAIL+1))
    fi
}

assert_contains() {
    local desc="$1" needle="$2" haystack="$3"
    if echo "$haystack" | grep -F -q "$needle"; then
        green "  ✅ $desc"
        PASS=$((PASS+1))
    else
        red "  ❌ $desc (expected to contain: $needle)"
        FAIL=$((FAIL+1))
    fi
}

echo ""
bold "========================================"
bold "  AI Language Coach — E2E Test Suite"
bold "========================================"
echo ""

# ── 1. Auth Service ──────────────────────────────────────
bold "[1/5] Auth Service (port 8081)"

# 1.1 Register a test user (idempotent)
E2E_EMAIL="e2e_$(date +%s)@test.com"
echo "  Registering $E2E_EMAIL..."
REG_RESP=$(curl -s -X POST "${BASE_URL}:8081/auth/register" \
    -H "Content-Type: application/json" \
    -d "{\"email\":\"${E2E_EMAIL}\",\"password\":\"Test1234!\",\"firstName\":\"E2E\",\"lastName\":\"Test\"}")
TOKEN=$(echo "$REG_RESP" | python3 -c "import sys,json; print(json.load(sys.stdin).get('accessToken',''))" 2>/dev/null || echo "")
assert_eq "1.1 POST /auth/register returns token" "1" "$( [ -n "$TOKEN" ] && echo 1 || echo 0 )"

# 1.2 Login with registered credentials
LOGIN_RESP=$(curl -s -X POST "${BASE_URL}:8081/auth/login" \
    -H "Content-Type: application/json" \
    -d "{\"email\":\"${E2E_EMAIL}\",\"password\":\"Test1234!\"}")
LOGIN_TOKEN=$(echo "$LOGIN_RESP" | python3 -c "import sys,json; print(json.load(sys.stdin).get('accessToken',''))" 2>/dev/null || echo "")
assert_eq "1.2 POST /auth/login returns token" "1" "$( [ -n "$LOGIN_TOKEN" ] && echo 1 || echo 0 )"

# 1.3 Validate
VALIDATE_RESP=$(curl -s "${BASE_URL}:8081/auth/validate" \
    -H "Authorization: Bearer $TOKEN")
assert_eq "1.3 GET /auth/validate returns true" "true" "$VALIDATE_RESP"

# 1.4 Login with wrong password
WRONG_LOGIN=$(curl -s -o /dev/null -w "%{http_code}" -X POST "${BASE_URL}:8081/auth/login" \
    -H "Content-Type: application/json" \
    -d "{\"email\":\"${E2E_EMAIL}\",\"password\":\"wrong\"}")
assert_eq "1.4 POST /auth/login wrong password returns 401" "401" "$WRONG_LOGIN"

# 1.5 Swagger
AUTH_SWAGGER=$(curl -s -o /dev/null -w "%{http_code}" "${BASE_URL}:8081/v3/api-docs")
assert_eq "1.5 GET /v3/api-docs (auth) returns 200" "200" "$AUTH_SWAGGER"

echo ""

# ── 2. Profile Service ───────────────────────────────────
bold "[2/5] Profile Service (port 8082)"

# 2.1 Create profile
CREATE_PROFILE=$(curl -s -o /dev/null -w "%{http_code}" -X POST "${BASE_URL}:8082/profile" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d '{"targetLanguage":"Czech","targetLevel":"A1","nativeLanguage":"en"}')
assert_eq "2.1 POST /profile creates profile" "200" "$CREATE_PROFILE"

# 2.2 Get profile
PROFILE_RESP=$(curl -s "${BASE_URL}:8082/profile" \
    -H "Authorization: Bearer $TOKEN")
PROFILE_LANG=$(echo "$PROFILE_RESP" | python3 -c "import sys,json; print(json.load(sys.stdin)['targetLanguage'])" 2>/dev/null || echo "")
assert_eq "2.2 GET /profile returns language=Czech" "Czech" "$PROFILE_LANG"

# 2.3 Update profile
UPDATE_RESP=$(curl -s -o /dev/null -w "%{http_code}" -X PUT "${BASE_URL}:8082/profile" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d '{"targetLanguage":"German","targetLevel":"B1"}')
assert_eq "2.3 PUT /profile updates profile" "200" "$UPDATE_RESP"

# 2.4 Verify update
PROFILE2=$(curl -s "${BASE_URL}:8082/profile" \
    -H "Authorization: Bearer $TOKEN")
PROFILE_LEVEL=$(echo "$PROFILE2" | python3 -c "import sys,json; print(json.load(sys.stdin)['targetLevel'])" 2>/dev/null || echo "")
assert_eq "2.4 GET /profile shows updated level=B1" "B1" "$PROFILE_LEVEL"

# 2.5 No auth = 401
NO_AUTH_PROFILE=$(curl -s -o /dev/null -w "%{http_code}" "${BASE_URL}:8082/profile")
assert_eq "2.5 GET /profile no auth returns 401" "401" "$NO_AUTH_PROFILE"

# 2.6 Swagger
PROFILE_SWAGGER=$(curl -s -o /dev/null -w "%{http_code}" "${BASE_URL}:8082/v3/api-docs")
assert_eq "2.6 GET /v3/api-docs (profile) returns 200" "200" "$PROFILE_SWAGGER"

echo ""

# ── 3. Diagnostic Service ────────────────────────────────
bold "[3/5] Diagnostic Service (port 8083)"

# 3.1 Start diagnostic
DIAG_START=$(curl -s -o /dev/null -w "%{http_code}" -X POST \
    "${BASE_URL}:8083/diagnostic/start?targetLanguage=Czech&targetLevel=A1" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d '{}')
assert_eq "3.1 POST /diagnostic/start returns 200" "200" "$DIAG_START"

# 3.2 Get game templates (public)
GAME_TEMPLATES=$(curl -s "${BASE_URL}:8083/api/v1/diagnostic/game/templates")
assert_contains "3.2 GET game/templates returns array" "[" "$GAME_TEMPLATES"

# 3.3 Game templates is public (no auth)
GAME_NO_AUTH=$(curl -s -o /dev/null -w "%{http_code}" "${BASE_URL}:8083/api/v1/diagnostic/game/templates")
assert_eq "3.3 GET game/templates no auth = 200 (public)" "200" "$GAME_NO_AUTH"

# 3.4 Diagnostic without auth = 401
DIAG_NO_AUTH=$(curl -s -o /dev/null -w "%{http_code}" -X POST \
    "${BASE_URL}:8083/diagnostic/start" \
    -H "Content-Type: application/json" -d '{}')
assert_eq "3.4 POST /diagnostic/start no auth = 401" "401" "$DIAG_NO_AUTH"

# 3.5 Swagger
DIAG_SWAGGER=$(curl -s -o /dev/null -w "%{http_code}" "${BASE_URL}:8083/v3/api-docs")
assert_eq "3.5 GET /v3/api-docs (diagnostic) returns 200" "200" "$DIAG_SWAGGER"

echo ""

# ── 4. Voice Service ─────────────────────────────────────
bold "[4/5] Voice Service (port 8084)"

# 4.1 Health check (may be restricted)
VOICE_HEALTH=$(curl -s -o /dev/null -w "%{http_code}" \
    "${BASE_URL}:8084/api/v1/voice/health" 2>/dev/null || echo "000")
assert_eq "4.1 GET /api/v1/voice/health returns 200" "200" "$VOICE_HEALTH"

# 4.2 Swagger
VOICE_SWAGGER=$(curl -s -o /dev/null -w "%{http_code}" "${BASE_URL}:8084/v3/api-docs")
assert_eq "4.2 GET /v3/api-docs (voice) returns 200" "200" "$VOICE_SWAGGER"

echo ""

# ── 5. Community Service ─────────────────────────────────
bold "[5/5] Community Service (port 8085)"

# 5.1 Status
COMM_STATUS=$(curl -s "${BASE_URL}:8085/community/status")
assert_contains "5.1 GET /community/status returns UP" "UP" "$COMM_STATUS"

# 5.2 List communities (using registered user's UUID)
STUDENT_USER_ID=$(echo "$REG_RESP" | python3 -c "import sys,json; print(json.load(sys.stdin).get('userId',''))" 2>/dev/null || echo "")
COMM_LIST=$(curl -s -o /dev/null -w "%{http_code}" "${BASE_URL}:8085/community/communities" \
    -H "X-User-Id: ${STUDENT_USER_ID}" \
    -H "X-Tenant-Id: SYSTEM" \
    -H "X-Role: USER_STUDENT")
assert_eq "5.2 GET /community/communities returns 200" "200" "$COMM_LIST"

# 5.3 Leaderboard
LEADERBOARD=$(curl -s -o /dev/null -w "%{http_code}" "${BASE_URL}:8085/community/leaderboard" \
    -H "X-Tenant-Id: SYSTEM")
assert_eq "5.3 GET /community/leaderboard returns 200" "200" "$LEADERBOARD"

# 5.4 Register as ADMIN_TEACHER for community tests
ADMIN_EMAIL="admin_e2e_$(date +%s)@test.com"
echo "  Registering $ADMIN_EMAIL as ADMIN_TEACHER..."
ADMIN_REG=$(curl -s -X POST "${BASE_URL}:8081/auth/register" \
    -H "Content-Type: application/json" \
    -d "{\"email\":\"${ADMIN_EMAIL}\",\"password\":\"Test1234!\",\"firstName\":\"Admin\",\"lastName\":\"E2E\",\"role\":\"ADMIN_TEACHER\"}")
ADMIN_TOKEN=$(echo "$ADMIN_REG" | python3 -c "import sys,json; print(json.load(sys.stdin).get('accessToken',''))" 2>/dev/null || echo "")
ADMIN_USER_ID=$(echo "$ADMIN_REG" | python3 -c "import sys,json; print(json.load(sys.stdin).get('userId',''))" 2>/dev/null || echo "")
assert_eq "5.4 Register ADMIN_TEACHER returns token" "1" "$( [ -n "$ADMIN_TOKEN" ] && echo 1 || echo 0 )"

# 5.5 Create community (ADMIN_TEACHER)
COMM_CREATE=$(curl -s -o /dev/null -w "%{http_code}" -X POST "${BASE_URL}:8085/community/communities" \
    -H "X-User-Id: ${ADMIN_USER_ID}" \
    -H "X-Tenant-Id: SYSTEM" \
    -H "X-Role: ADMIN_TEACHER" \
    -H "Content-Type: application/json" \
    -d '{"name":"E2E Test Community","description":"Created during E2E tests"}')
assert_eq "5.5 POST /community/communities create returns 200" "200" "$COMM_CREATE"

# 5.6 Get created community ID
COMM_LIST_RESP=$(curl -s "${BASE_URL}:8085/community/communities" \
    -H "X-User-Id: ${ADMIN_USER_ID}" \
    -H "X-Tenant-Id: SYSTEM" \
    -H "X-Role: ADMIN_TEACHER")
COMM_ID=$(echo "$COMM_LIST_RESP" | python3 -c "import sys,json; d=json.load(sys.stdin); content=d.get('content',[]); print(content[0]['id'] if content else '')" 2>/dev/null || echo "")
assert_eq "5.6 GET communities has content" "1" "$( [ -n "$COMM_ID" ] && echo 1 || echo 0 )"

# 5.7 Join community as student
if [ -n "$COMM_ID" ] && [ -n "$STUDENT_USER_ID" ]; then
    COMM_JOIN=$(curl -s -o /dev/null -w "%{http_code}" -X POST "${BASE_URL}:8085/community/communities/${COMM_ID}/join" \
        -H "X-User-Id: ${STUDENT_USER_ID}")
    assert_eq "5.7 POST join community returns 200" "200" "$COMM_JOIN"

    # 5.8 Duplicate join returns 409
    COMM_JOIN2=$(curl -s -o /dev/null -w "%{http_code}" -X POST "${BASE_URL}:8085/community/communities/${COMM_ID}/join" \
        -H "X-User-Id: ${STUDENT_USER_ID}")
    assert_eq "5.8 POST duplicate join returns 409" "409" "$COMM_JOIN2"

    # 5.9 Leave community
    COMM_LEAVE=$(curl -s -o /dev/null -w "%{http_code}" -X POST "${BASE_URL}:8085/community/communities/${COMM_ID}/leave" \
        -H "X-User-Id: ${STUDENT_USER_ID}")
    assert_eq "5.9 POST leave community returns 204" "204" "$COMM_LEAVE"
fi

# 5.10 Swagger
COMM_SWAGGER=$(curl -s -o /dev/null -w "%{http_code}" "${BASE_URL}:8085/v3/api-docs")
assert_eq "5.10 GET /v3/api-docs (community) returns 200" "200" "$COMM_SWAGGER"

echo ""

# ── 6. Game Service ───────────────────────────────────────
bold "[6/6] Game Service (port 8083)"

# 6.1 Start a game session
GAME_START=$(curl -s -o /dev/null -w "%{http_code}" -X POST \
    "${BASE_URL}:8083/api/v1/diagnostic/game/start?userId=${STUDENT_USER_ID}&templateId=standard&targetLanguage=Czech&targetLevel=A1" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d '{}')
assert_eq "6.1 POST game/start returns 200" "200" "$GAME_START"

# 6.2 Create admin template (requires auth token)
CREATE_TEMPLATE=$(curl -s -o /dev/null -w "%{http_code}" -X POST \
    "${BASE_URL}:8083/api/v1/diagnostic/game/admin/templates" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d '{"templateId":"e2e_test","displayName":"E2E Test","templateCategory":"GRAMMAR","displayOrder":99,"description":"E2E test template","minQuestions":3,"maxQuestions":10}')
assert_eq "6.2 POST game/admin/templates returns 200" "200" "$CREATE_TEMPLATE"

echo ""

# ── Summary ──────────────────────────────────────────────
bold "========================================"
bold "  Results: $PASS passed, $FAIL failed"
bold "========================================"

if [ "$FAIL" -gt 0 ]; then
    echo ""
    red "Some tests failed. Check the output above for details."
    exit 1
else
    green "All tests passed!"
    exit 0
fi
