#!/bin/bash

# Test script for endpoint access control
# Usage: ./test-endpoints.sh

BASE_URL="http://localhost:8080"

echo "🧪 Testing Endpoint Access Control"
echo "=================================="

# Test public endpoints (should work without auth)
echo "🔓 Testing PUBLIC endpoints..."
curl -s -o /dev/null -w "GET /api/products: %{http_code}\n" "$BASE_URL/api/products"
curl -s -o /dev/null -w "GET /api/auth/debug/cookies: %{http_code}\n" "$BASE_URL/api/auth/debug/cookies"

# Test authenticated endpoints (should require auth)
echo "🔐 Testing AUTHENTICATED endpoints..."
curl -s -o /dev/null -w "GET /api/auth/check-admin: %{http_code}\n" "$BASE_URL/api/auth/check-admin"
curl -s -o /dev/null -w "GET /api/profile: %{http_code}\n" "$BASE_URL/api/profile"

# Test admin-only endpoints (should require admin role)
echo "👑 Testing ADMIN-ONLY endpoints..."
curl -s -o /dev/null -w "GET /api/admin/users: %{http_code}\n" "$BASE_URL/api/admin/users"
curl -s -o /dev/null -w "GET /swagger-ui.html: %{http_code}\n" "$BASE_URL/swagger-ui.html"

echo "✅ Test complete!"
echo ""
echo "Expected results:"
echo "- Public endpoints: 200"
echo "- Authenticated endpoints: 401 (without auth)"
echo "- Admin endpoints: 401 (without auth)"


