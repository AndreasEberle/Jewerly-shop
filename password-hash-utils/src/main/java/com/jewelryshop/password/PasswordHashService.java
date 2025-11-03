package com.jewelryshop.password;

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * Service class for password hashing operations
 * This is a Spring service that can be injected into other components
 */
@Service
public class PasswordHashService {
    
    private final PasswordHashGenerator passwordHashGenerator;
    
    public PasswordHashService() {
        this.passwordHashGenerator = new PasswordHashGenerator();
    }
    
    public PasswordHashService(int strength) {
        this.passwordHashGenerator = new PasswordHashGenerator(strength);
    }
    
    /**
     * Generate a hash for a single password
     * 
     * @param password the plain text password
     * @return the BCrypt hash
     */
    public String hashPassword(String password) {
        return passwordHashGenerator.generateHash(password);
    }
    
    /**
     * Generate hashes for multiple passwords
     * 
     * @param passwords list of plain text passwords
     * @return map of password -> hash
     */
    public Map<String, String> hashPasswords(List<String> passwords) {
        return passwordHashGenerator.generateHashes(passwords);
    }
    
    /**
     * Generate hashes for multiple passwords and return as PasswordHash objects
     * 
     * @param passwords list of plain text passwords
     * @return list of PasswordHash objects
     */
    public List<PasswordHash> generatePasswordHashes(List<String> passwords) {
        return passwordHashGenerator.generatePasswordHashes(passwords);
    }
    
    /**
     * Verify a password against a hash
     * 
     * @param password the plain text password
     * @param hash the BCrypt hash
     * @return true if password matches hash
     */
    public boolean verifyPassword(String password, String hash) {
        return passwordHashGenerator.verifyPassword(password, hash);
    }
    
    /**
     * Generate hashes for common passwords
     * 
     * @return map of common passwords and their hashes
     */
    public Map<String, String> generateCommonPasswords() {
        return passwordHashGenerator.generateCommonPasswords();
    }
    
    /**
     * Generate hashes for a custom list of passwords
     * 
     * @param passwords list of passwords to hash
     * @return map of password -> hash
     */
    public Map<String, String> generateCustomPasswords(List<String> passwords) {
        return passwordHashGenerator.generateHashes(passwords);
    }
}





