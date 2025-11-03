package com.jewelryshop.password;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * REST Controller for password hashing operations
 * Provides HTTP endpoints for generating and verifying password hashes
 */
@RestController
@RequestMapping("/api/password")
public class PasswordHashController {
    
    private final PasswordHashService passwordHashService;
    
    public PasswordHashController(PasswordHashService passwordHashService) {
        this.passwordHashService = passwordHashService;
    }
    
    /**
     * Generate hash for a single password
     * 
     * @param request containing the password
     * @return the generated hash
     */
    @PostMapping("/hash")
    public ResponseEntity<Map<String, String>> hashPassword(@RequestBody PasswordRequest request) {
        try {
            String hash = passwordHashService.hashPassword(request.getPassword());
            Map<String, String> response = new HashMap<>();
            response.put("password", request.getPassword());
            response.put("hash", hash);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Generate hashes for multiple passwords
     * 
     * @param request containing the list of passwords
     * @return map of password -> hash
     */
    @PostMapping("/hash-multiple")
    public ResponseEntity<Map<String, String>> hashPasswords(@RequestBody PasswordListRequest request) {
        try {
            Map<String, String> hashes = passwordHashService.hashPasswords(request.getPasswords());
            return ResponseEntity.ok(hashes);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Verify a password against a hash
     * 
     * @param request containing password and hash
     * @return verification result
     */
    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyPassword(@RequestBody VerifyRequest request) {
        try {
            boolean matches = passwordHashService.verifyPassword(request.getPassword(), request.getHash());
            Map<String, Object> response = new HashMap<>();
            response.put("password", request.getPassword());
            response.put("hash", request.getHash());
            response.put("matches", matches);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Generate hashes for common passwords
     * 
     * @return map of common passwords and their hashes
     */
    @GetMapping("/common")
    public ResponseEntity<Map<String, String>> generateCommonPasswords() {
        try {
            Map<String, String> hashes = passwordHashService.generateCommonPasswords();
            return ResponseEntity.ok(hashes);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    // Request DTOs
    public static class PasswordRequest {
        private String password;
        
        public String getPassword() {
            return password;
        }
        
        public void setPassword(String password) {
            this.password = password;
        }
    }
    
    public static class PasswordListRequest {
        private List<String> passwords;
        
        public List<String> getPasswords() {
            return passwords;
        }
        
        public void setPasswords(List<String> passwords) {
            this.passwords = passwords;
        }
    }
    
    public static class VerifyRequest {
        private String password;
        private String hash;
        
        public String getPassword() {
            return password;
        }
        
        public void setPassword(String password) {
            this.password = password;
        }
        
        public String getHash() {
            return hash;
        }
        
        public void setHash(String hash) {
            this.hash = hash;
        }
    }
}





