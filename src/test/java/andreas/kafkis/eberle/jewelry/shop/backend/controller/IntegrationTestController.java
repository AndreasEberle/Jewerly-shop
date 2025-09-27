package andreas.kafkis.eberle.jewelry.shop.backend.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.User;
import andreas.kafkis.eberle.jewelry.shop.backend.service.EmailService;
import andreas.kafkis.eberle.jewelry.shop.backend.service.StorageService;

/**
 * Test controller for integration testing
 * Only available in test profile
 */
@RestController
@RequestMapping("/api/test")
public class IntegrationTestController {

    @Autowired
    private EmailService emailService;

    @Autowired
    private StorageService storageService;

    @Value("${email.test-to:}")
    private String testEmailTo;

    @Value("${storage.type:local}")
    private String storageType;

    @Value("${spring.security.oauth2.client.registration.google.client-id:}")
    private String googleClientId;

    /**
     * Test email connectivity
     */
    @PostMapping("/email/test")
    public ResponseEntity<Map<String, Object>> testEmail(@RequestParam(required = false) String to) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String testEmail = to != null ? to : testEmailTo;
            if (testEmail == null || testEmail.isEmpty()) {
                result.put("success", false);
                result.put("error", "No test email configured. Set email.test-to in properties");
                return ResponseEntity.badRequest().body(result);
            }

            // Create a test user
            User testUser = new User();
            testUser.setId(UUID.randomUUID());
            testUser.setEmail(testEmail);
            testUser.setFirstName("Test");
            testUser.setLastName("User");

            // Send welcome email
            emailService.sendWelcomeEmail(testUser);
            
            result.put("success", true);
            result.put("message", "Test email sent successfully to: " + testEmail);
            result.put("emailFrom", "Configured in email.from property");
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "Failed to send test email: " + e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }

    /**
     * Test storage connectivity
     */
    @PostMapping("/storage/test")
    public ResponseEntity<Map<String, Object>> testStorage() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            result.put("storageType", storageType);
            result.put("storageInfo", storageService.getStorageInfo());
            
            if ("s3".equals(storageType)) {
                result.put("success", true);
                result.put("message", "S3 storage configured. Bucket connectivity test not implemented yet.");
            } else {
                result.put("success", true);
                result.put("message", "Local storage configured. No connectivity test needed.");
            }
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "Storage test failed: " + e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }

    /**
     * Test Google OAuth configuration
     */
    @GetMapping("/oauth/test")
    public ResponseEntity<Map<String, Object>> testOAuth() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            result.put("googleClientId", googleClientId);
            result.put("configured", !googleClientId.isEmpty() && !googleClientId.equals("test-client-id"));
            
            if (googleClientId.isEmpty() || googleClientId.equals("test-client-id")) {
                result.put("success", false);
                result.put("message", "Google OAuth not configured. Using test values.");
            } else {
                result.put("success", true);
                result.put("message", "Google OAuth configured with client ID: " + googleClientId);
            }
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "OAuth test failed: " + e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }

    /**
     * Get current configuration status
     */
    @GetMapping("/config/status")
    public ResponseEntity<Map<String, Object>> getConfigStatus() {
        Map<String, Object> result = new HashMap<>();
        
        result.put("email", Map.of(
            "testTo", testEmailTo != null ? testEmailTo : "Not configured",
            "configured", testEmailTo != null && !testEmailTo.isEmpty()
        ));
        
        result.put("storage", Map.of(
            "type", storageType,
            "configured", !"local".equals(storageType) || storageType != null
        ));
        
        result.put("oauth", Map.of(
            "googleClientId", googleClientId,
            "configured", !googleClientId.isEmpty() && !googleClientId.equals("test-client-id")
        ));
        
        return ResponseEntity.ok(result);
    }
}
