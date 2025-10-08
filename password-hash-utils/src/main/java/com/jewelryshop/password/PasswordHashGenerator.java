package com.jewelryshop.password;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 * Password Hash Generator Utility
 * 
 * This utility class provides methods to generate BCrypt hashes for passwords.
 * It can process single passwords or lists of passwords.
 * 
 * Usage:
 * 1. Create an instance: PasswordHashGenerator generator = new PasswordHashGenerator();
 * 2. Generate single hash: String hash = generator.generateHash("password123");
 * 3. Generate multiple hashes: Map<String, String> hashes = generator.generateHashes(List.of("pass1", "pass2"));
 * 4. Verify password: boolean matches = generator.verifyPassword("password123", hash);
 */
public class PasswordHashGenerator {
    
    private final BCryptPasswordEncoder passwordEncoder;
    
    public PasswordHashGenerator() {
        this.passwordEncoder = new BCryptPasswordEncoder();
    }
    
    public PasswordHashGenerator(int strength) {
        this.passwordEncoder = new BCryptPasswordEncoder(strength);
    }
    
    /**
     * Generate a BCrypt hash for a single password
     * 
     * @param password the plain text password
     * @return the BCrypt hash
     */
    public String generateHash(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        return passwordEncoder.encode(password);
    }
    
    /**
     * Generate BCrypt hashes for a list of passwords
     * 
     * @param passwords list of plain text passwords
     * @return map of password -> hash
     */
    public Map<String, String> generateHashes(List<String> passwords) {
        if (passwords == null || passwords.isEmpty()) {
            throw new IllegalArgumentException("Passwords list cannot be null or empty");
        }
        
        Map<String, String> hashes = new HashMap<>();
        for (String password : passwords) {
            if (password != null && !password.trim().isEmpty()) {
                hashes.put(password, generateHash(password));
            }
        }
        return hashes;
    }
    
    /**
     * Generate BCrypt hashes for a list of passwords and return as a list of PasswordHash objects
     * 
     * @param passwords list of plain text passwords
     * @return list of PasswordHash objects
     */
    public List<PasswordHash> generatePasswordHashes(List<String> passwords) {
        if (passwords == null || passwords.isEmpty()) {
            throw new IllegalArgumentException("Passwords list cannot be null or empty");
        }
        
        List<PasswordHash> passwordHashes = new ArrayList<>();
        for (String password : passwords) {
            if (password != null && !password.trim().isEmpty()) {
                String hash = generateHash(password);
                passwordHashes.add(new PasswordHash(password, hash));
            }
        }
        return passwordHashes;
    }
    
    /**
     * Verify a password against a hash
     * 
     * @param password the plain text password
     * @param hash the BCrypt hash
     * @return true if password matches hash
     */
    public boolean verifyPassword(String password, String hash) {
        if (password == null || hash == null) {
            return false;
        }
        return passwordEncoder.matches(password, hash);
    }
    
    /**
     * Generate hashes for common passwords
     * 
     * @return map of common passwords and their hashes
     */
    public Map<String, String> generateCommonPasswords() {
        List<String> commonPasswords = List.of(
            "password",
            "password123",
            "admin123",
            "test123",
            "user123",
            "123456",
            "qwerty",
            "letmein",
            "welcome",
            "monkey",
            "password1234",
            "admin",
            "test",
            "user",
            "guest",
            "root",
            "toor",
            "pass",
            "secret",
            "login"
        );
        
        return generateHashes(commonPasswords);
    }
    
    /**
     * Print hashes in a formatted way
     * 
     * @param hashes map of password -> hash
     */
    public void printHashes(Map<String, String> hashes) {
        System.out.println("=== Generated Password Hashes ===");
        for (Map.Entry<String, String> entry : hashes.entrySet()) {
            System.out.println(entry.getKey() + " -> " + entry.getValue());
        }
        System.out.println();
    }
    
    /**
     * Print hashes in SQL format for database insertion
     * 
     * @param hashes map of password -> hash
     * @param tableName the database table name
     * @param emailColumn the email column name
     * @param passwordColumn the password column name
     */
    public void printHashesAsSQL(Map<String, String> hashes, String tableName, String emailColumn, String passwordColumn) {
        System.out.println("=== SQL INSERT Statements ===");
        for (Map.Entry<String, String> entry : hashes.entrySet()) {
            String email = entry.getKey() + "@example.com";
            String hash = entry.getValue();
            System.out.println(String.format("INSERT INTO %s (%s, %s) VALUES ('%s', '%s');", 
                tableName, emailColumn, passwordColumn, email, hash));
        }
        System.out.println();
    }
}


