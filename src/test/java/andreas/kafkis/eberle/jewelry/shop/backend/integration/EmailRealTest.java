package andreas.kafkis.eberle.jewelry.shop.backend.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.User;
import andreas.kafkis.eberle.jewelry.shop.backend.service.EmailService;

/**
 * Real email test that shows how properties are loaded and can send real emails
 */
@SpringBootTest
@ActiveProfiles("testreal")
class EmailRealTest {

    @Autowired
    private EmailService emailService;

    @Value("${email.from:}")
    private String fromEmail;

    @Value("${email.admin:}")
    private String adminEmail;

    @Value("${email.test-to:}")
    private String testEmailTo;

    @Value("${spring.mail.host:}")
    private String mailHost;

    @Value("${spring.mail.port:0}")
    private int mailPort;

    @Value("${spring.mail.username:}")
    private String mailUsername;
    
    @Value("${email.first.name:}")
    private String emailFirstName;
    
    @Value("${email.last.name:}")
    private String emaillastName;

    @Test
    void testPropertiesAreLoadedFromTestStorageProfile() {
        System.out.println("=== Properties Loading Test ===");
        System.out.println("From Email: " + fromEmail);
        System.out.println("Admin Email: " + adminEmail);
        System.out.println("Test Email To: " + testEmailTo);
        System.out.println("Mail Host: " + mailHost);
        System.out.println("Mail Port: " + mailPort);
        System.out.println("Mail Username: " + mailUsername);
        
        // These should match the values from application-test-real.properties
        assertEquals("andreas.kafkis.eberle@gmail.com", fromEmail);
        assertEquals("andreas.kafkis.eberle@gmail.com", adminEmail);
        assertEquals("andreas.kafkis.eberle@gmail.com", testEmailTo);
        assertEquals("smtp.gmail.com", mailHost);
        assertEquals(587, mailPort);
        assertEquals("andreas.kafkis.eberle@gmail.com", mailUsername);
        
        System.out.println("✅ Properties loaded correctly from test-real profile!");
    }

    @Test
    void testEmailServiceWithRealConfiguration() {
        // This test uses the real EmailService with mocked JavaMailSender
        // The properties are loaded from application-test-real.properties
        User testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setEmail(testEmailTo);
        testUser.setFirstName(emailFirstName);
        testUser.setLastName(emaillastName);

        try {
            // This will use the mocked JavaMailSender, so no real email is sent
            emailService.sendWelcomeEmail(testUser);
            System.out.println("✅ Email service works with loaded properties (mocked)");
        } catch (Exception e) {
            System.out.println("❌ Email service failed: " + e.getMessage());
            fail("Email service should work with loaded properties");
        }
    }

    @Test
    void testPropertyOverrideHierarchy() {
        // This demonstrates how Spring Boot loads properties in order:
        // 1. application.properties (default)
        // 2. application-{profile}.properties (profile-specific)
        // 3. Environment variables
        // 4. Command line arguments
        
        System.out.println("=== Property Override Hierarchy ===");
        System.out.println("1. Default properties: application.properties");
        System.out.println("2. Profile properties: application-test-real.properties");
        System.out.println("3. Environment variables: (if set)");
        System.out.println("4. Command line: (if set)");
        System.out.println();
        System.out.println("Current values (from test-real profile):");
        System.out.println("  email.from = " + fromEmail);
        System.out.println("  email.admin = " + adminEmail);
        System.out.println("  email.test-to = " + testEmailTo);
    }
}
