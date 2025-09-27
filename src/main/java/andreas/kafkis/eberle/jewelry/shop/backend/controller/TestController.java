package andreas.kafkis.eberle.jewelry.shop.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import andreas.kafkis.eberle.jewelry.shop.backend.service.EmailService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestController {

    private final EmailService emailService;

    /**
     * Test email sending
     */
    @PostMapping("/email")
    public ResponseEntity<String> testEmail(@RequestParam String to) {
        try {
            emailService.sendSimpleEmail(
                to, 
                "Test Email from Jewelry Shop", 
                "This is a test email to verify SMTP configuration is working!"
            );
            return ResponseEntity.ok("Test email sent successfully to: " + to);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to send email: " + e.getMessage());
        }
    }

    /**
     * Test email with HTML template
     */
    @PostMapping("/email/welcome")
    public ResponseEntity<String> testWelcomeEmail(@RequestParam String to, @RequestParam String name) {
        try {
            // Create a test user for welcome email
            andreas.kafkis.eberle.jewelry.shop.backend.entities.User testUser = 
                andreas.kafkis.eberle.jewelry.shop.backend.entities.User.builder()
                    .email(to)
                    .firstName(name)
                    .lastName("Test")
                    .build();
                    
            emailService.sendWelcomeEmail(testUser);
            return ResponseEntity.ok("Welcome email sent successfully to: " + to);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to send welcome email: " + e.getMessage());
        }
    }
}
