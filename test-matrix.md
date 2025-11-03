# 🧪 Endpoint Testing Matrix

## Test Scenarios

### Scenario 1: Anonymous User
- **Login:** None
- **Expected:** Public endpoints work, others return 401/403

### Scenario 2: Customer User
- **Login:** OAuth2 with customer account
- **Expected:** Public + Authenticated + Customer endpoints work, Admin endpoints return 403

### Scenario 3: Admin User
- **Login:** OAuth2 with admin account
- **Expected:** All endpoints work

## Quick Test URLs

### Public (Should work for everyone)
```
✅ http://localhost:8080/api/products
✅ http://localhost:8080/api/auth/debug/cookies
✅ http://localhost:8080/static/oauth2-success.html
```

### Authenticated (Should work for logged-in users)
```
🔐 http://localhost:8080/api/auth/check-admin
🔐 http://localhost:8080/api/profile
```

### Customer + Admin (Should work for both roles)
```
👥 http://localhost:8080/api/cart
👥 http://localhost:8080/api/orders
```

### Admin Only (Should work only for admin)
```
👑 http://localhost:8080/api/admin/users
👑 http://localhost:8080/swagger-ui.html
👑 http://localhost:8080/api/health
```

## Expected HTTP Status Codes

| Endpoint Type | Anonymous | Customer | Admin |
|---------------|-----------|----------|-------|
| Public        | 200       | 200      | 200   |
| Authenticated | 401       | 200      | 200   |
| Customer+Admin| 401       | 200      | 200   |
| Admin Only    | 401       | 403      | 200   |





