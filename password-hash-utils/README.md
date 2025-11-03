# Password Hash Generator

A standalone Spring Boot application for generating BCrypt password hashes. This utility can process single passwords or lists of passwords and generate their corresponding BCrypt hashes.

## Features

- Generate BCrypt hashes for single passwords
- Generate BCrypt hashes for lists of passwords
- Verify passwords against hashes
- Generate hashes for common passwords
- REST API endpoints for programmatic access
- Command line interface for batch processing
- SQL output format for database insertion

## Usage

### 1. Running the Application

```bash
# Compile and run
mvn clean compile
mvn spring-boot:run

# Or build and run JAR
mvn clean package
java -jar target/password-hash-utils-1.0.0.jar
```

### 2. Programmatic Usage

```java
// Create a service instance
PasswordHashService service = new PasswordHashService();

// Generate hash for single password
String hash = service.hashPassword("password123");

// Generate hashes for multiple passwords
List<String> passwords = List.of("password123", "admin123", "test123");
Map<String, String> hashes = service.hashPasswords(passwords);

// Verify a password
boolean matches = service.verifyPassword("password123", hash);
```

### 3. REST API Usage

The application provides REST endpoints:

- `POST /api/password/hash` - Hash a single password
- `POST /api/password/hash-multiple` - Hash multiple passwords
- `POST /api/password/verify` - Verify a password against a hash
- `GET /api/password/common` - Get hashes for common passwords

#### Example API Calls

```bash
# Hash a single password
curl -X POST http://localhost:8081/password-hash/api/password/hash \
  -H "Content-Type: application/json" \
  -d '{"password": "password123"}'

# Hash multiple passwords
curl -X POST http://localhost:8081/password-hash/api/password/hash-multiple \
  -H "Content-Type: application/json" \
  -d '{"passwords": ["password123", "admin123", "test123"]}'

# Verify a password
curl -X POST http://localhost:8081/password-hash/api/password/verify \
  -H "Content-Type: application/json" \
  -d '{"password": "password123", "hash": "$2a$10$..."}'
```

### 4. Command Line Usage

When you run the application, it will automatically generate hashes for common passwords and display them in the console. You can also modify the `PasswordHashRunner` class to process your custom password lists.

## Configuration

The application uses the following default configuration:

- **Server Port**: 8081
- **Context Path**: /password-hash
- **BCrypt Strength**: 10 (default)
- **Logging Level**: DEBUG

You can modify these settings in `src/main/resources/application.properties`.

## Dependencies

- Spring Boot 3.2.0
- Spring Security (for BCrypt support)
- Maven 3.6+

## Project Structure

```
src/main/java/com/jewelryshop/password/
├── PasswordHashApplication.java      # Main Spring Boot application
├── PasswordHashController.java       # REST API endpoints
├── PasswordHashService.java          # Service layer
├── PasswordHashGenerator.java        # Core hashing logic
├── PasswordHash.java                 # Data class
└── PasswordHashRunner.java           # Command line runner
```

## Examples

### Generate Hashes for Custom Passwords

```java
List<String> myPasswords = List.of(
    "password1234",
    "admin123",
    "test123",
    "user123",
    "mypassword"
);

PasswordHashService service = new PasswordHashService();
Map<String, String> hashes = service.hashPasswords(myPasswords);

// Print results
for (Map.Entry<String, String> entry : hashes.entrySet()) {
    System.out.println(entry.getKey() + " -> " + entry.getValue());
}
```

### Generate SQL INSERT Statements

```java
Map<String, String> hashes = service.hashPasswords(passwords);

System.out.println("-- SQL INSERT Statements");
for (Map.Entry<String, String> entry : hashes.entrySet()) {
    String email = entry.getKey() + "@example.com";
    String hash = entry.getValue();
    System.out.println(String.format(
        "INSERT INTO users (email, password_hash) VALUES ('%s', '%s');", 
        email, hash
    ));
}
```

## Security Notes

- This utility is designed for development and testing purposes
- Never use this in production without proper security measures
- The generated hashes are suitable for database storage
- Always use HTTPS in production environments

## License

This project is part of the Jewelry Shop application and follows the same license terms.





