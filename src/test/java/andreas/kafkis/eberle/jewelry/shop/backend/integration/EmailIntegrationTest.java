package andreas.kafkis.eberle.jewelry.shop.backend.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
 * Integration test for email functionality using real properties
 */
@SpringBootTest
@ActiveProfiles("test")
class EmailIntegrationTest {

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

    @Test
    void testEmailConfiguration() {
        System.out.println("=== Email Configuration Test ===");
        System.out.println("From Email: " + fromEmail);
        System.out.println("Admin Email: " + adminEmail);
        System.out.println("Test Email To: " + testEmailTo);
        System.out.println("Mail Host: " + mailHost);
        System.out.println("Mail Port: " + mailPort);
        System.out.println("Mail Username: " + mailUsername);
        
        // Verify properties are loaded from application-test.properties
        assertEquals("test@jewelryshop.com", fromEmail);
        assertEquals("admin@jewelryshop.com", adminEmail);
        assertEquals("test@example.com", testEmailTo);
        assertEquals("localhost", mailHost);
        assertEquals(587, mailPort);
        assertEquals("test@example.com", mailUsername);
    }

    @Test
    void testEmailServiceInjection() {
        assertNotNull(emailService, "EmailService should be injected");
        System.out.println("EmailService injected successfully: " + emailService.getClass().getSimpleName());
    }

    @Test
    void testEmailServiceWithMockedDependencies() {
        // This test will use the mocked JavaMailSender from the test configuration
        User testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");

        try {
            // This should work with mocked dependencies
            emailService.sendWelcomeEmail(testUser);
            System.out.println("✅ Welcome email test completed (mocked)");
        } catch (Exception e) {
            System.out.println("❌ Welcome email test failed: " + e.getMessage());
            fail("Email service should work with mocked dependencies");
        }
    }

    @Test
    void testEmailPropertiesOverride() {
        // Test that properties from application-test.properties override default values
        assertFalse(fromEmail.isEmpty(), "From email should be loaded from properties");
        assertFalse(adminEmail.isEmpty(), "Admin email should be loaded from properties");
        assertFalse(testEmailTo.isEmpty(), "Test email should be loaded from properties");
        
        System.out.println("✅ All email properties loaded successfully from application-test.properties");
    }
}
