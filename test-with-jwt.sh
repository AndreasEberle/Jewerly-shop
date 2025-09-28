#!/bin/bash

# Test script with JWT token authentication
# Usage: ./test-with-jwt.sh <JWT_TOKEN>

BASE_URL="http://localhost:8080"
JWT_TOKEN=$1

if [ -z "$JWT_TOKEN" ]; then
    echo "❌ Usage: ./test-with-jwt.sh <JWT_TOKEN>"
    echo "Get JWT token from: http://localhost:8080/api/auth/debug/cookies"
    exit 1
fi

echo "🧪 Testing with JWT Token: ${JWT_TOKEN:0:20}..."
echo "=============================================="

# Test with JWT token
echo "🔐 Testing AUTHENTICATED endpoints with JWT..."
curl -s -H "Authorization: Bearer $JWT_TOKEN" -o /dev/null -w "GET /api/auth/check-admin: %{http_code}\n" "$BASE_URL/api/auth/check-admin"
curl -s -H "Authorization: Bearer $JWT_TOKEN" -o /dev/null -w "GET /api/profile: %{http_code}\n" "$BASE_URL/api/profile"

echo "👑 Testing ADMIN-ONLY endpoints with JWT..."
curl -s -H "Authorization: Bearer $JWT_TOKEN" -o /dev/null -w "GET /api/admin/users: %{http_code}\n" "$BASE_URL/api/admin/users"
curl -s -H "Authorization: Bearer $JWT_TOKEN" -o /dev/null -w "GET /swagger-ui.html: %{http_code}\n" "$BASE_URL/swagger-ui.html"

echo "✅ JWT test complete!"
