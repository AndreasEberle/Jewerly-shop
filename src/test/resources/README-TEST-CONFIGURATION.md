# Test Configuration Guide

This directory contains test configuration files following the same pattern as the main application.

## File Structure

```
src/test/resources/
├── application-test.properties          # Template (pushed to git)
├── application-test-real.properties     # Real values (NOT pushed to git)
├── test-data.sql                       # Test data for database
├── test-data-simple.sql               # Simple test data
└── README-TEST-CONFIGURATION.md        # This file
```

## Configuration Pattern

### 1. Template File (application-test.properties)
- **Purpose**: Template with placeholder values
- **Git Status**: ✅ Pushed to git
- **Usage**: Default test configuration
- **Values**: Placeholder values like `your-test-email@gmail.com`

### 2. Real Values File (application-test-real.properties)
- **Purpose**: Contains actual credentials and real values
- **Git Status**: ❌ NOT pushed to git (in .gitignore)
- **Usage**: For tests that need real external services
- **Values**: Your actual email, AWS credentials, etc.

## How to Use

### Option 1: Use Template (Default)
```java
@SpringBootTest
@ActiveProfiles("test")  // Loads application-test.properties
class MyTest {
    // Uses template values
}
```

### Option 2: Use Real Values
```java
@SpringBootTest
@ActiveProfiles("test-real")  // Loads application-test-real.properties
class MyTest {
    // Uses real values
}
```

## Setting Up Real Values

1. **Copy the template:**
   ```bash
   cp application-test.properties application-test-real.properties
   ```

2. **Edit application-test-real.properties:**
   ```properties
   # Replace placeholder values with real ones
   email.test-to=your-actual-email@gmail.com
   spring.mail.username=your-actual-email@gmail.com
   spring.mail.password=your-actual-app-password
   storage.s3.access-key=your-actual-aws-access-key
   storage.s3.secret-key=your-actual-aws-secret-key
   # ... etc
   ```

3. **Use in tests:**
   ```java
   @ActiveProfiles("test-real")
   ```

## Security Notes

- ✅ `application-test.properties` is safe to push (contains only placeholders)
- ❌ `application-test-real.properties` is gitignored (contains real credentials)
- 🔒 Never commit real credentials to git
- 🔄 Each developer should create their own `application-test-real.properties`

## Available Configurations

### Email Testing
- **Template**: Uses localhost SMTP (mocked)
- **Real**: Uses Gmail SMTP with app password

### Storage Testing
- **Template**: Uses local file storage
- **Real**: Uses AWS S3 with real credentials

### OAuth Testing
- **Template**: Uses mocked Google OAuth
- **Real**: Uses real Google OAuth client credentials

### Database Testing
- **Both**: Uses H2 in-memory database (no real credentials needed)

## Example Test Classes

- `PropertyLoadingTest` - Demonstrates template loading
- `PropertyLoadingRealTest` - Demonstrates real values loading
- `EmailRealTest` - Tests email configuration and property loading (supports both template and real values)

## Troubleshooting

### Properties Not Loading
- Check `@ActiveProfiles` annotation
- Ensure profile name matches file name
- Verify file is in `src/test/resources/`

### Real Values Not Working
- Check if `application-test-real.properties` exists
- Verify it's not empty
- Ensure values are not still placeholders

### Git Issues
- `application-test-real.properties` should be in .gitignore
- Only `application-test.properties` should be committed
