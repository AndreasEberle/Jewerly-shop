package com.jewelryshop.password;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;

/**
 * Command line runner for generating password hashes
 * This will run when the application starts and generate hashes for common passwords
 */
@Component
public class PasswordHashRunner implements CommandLineRunner {
    
    private final PasswordHashService passwordHashService;
    
    public PasswordHashRunner(PasswordHashService passwordHashService) {
        this.passwordHashService = passwordHashService;
    }
    
    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== Password Hash Generator Started ===");
        
        // Generate hashes for common passwords
        Map<String, String> commonHashes = passwordHashService.generateCommonPasswords();
        
        System.out.println("Common passwords and their hashes:");
        for (Map.Entry<String, String> entry : commonHashes.entrySet()) {
            System.out.println(entry.getKey() + " -> " + entry.getValue());
        }
        
        System.out.println("\n=== Custom Password Generation ===");
        
        // Example: Generate hashes for custom passwords
        List<String> customPasswords = List.of(
            "password1234",
            "admin123",
            "test123",
            "user123",
            "mypassword"
        );
        
        Map<String, String> customHashes = passwordHashService.hashPasswords(customPasswords);
        
        System.out.println("Custom passwords and their hashes:");
        for (Map.Entry<String, String> entry : customHashes.entrySet()) {
            System.out.println(entry.getKey() + " -> " + entry.getValue());
        }
        
        System.out.println("\n=== SQL INSERT Statements ===");
        System.out.println("-- Use these in your database migrations:");
        for (Map.Entry<String, String> entry : customHashes.entrySet()) {
            String email = entry.getKey() + "@example.com";
            String hash = entry.getValue();
            System.out.println(String.format("INSERT INTO users (email, password_hash) VALUES ('%s', '%s');", 
                email, hash));
        }
        
        System.out.println("\n=== Password Hash Generator Complete ===");
    }
}
