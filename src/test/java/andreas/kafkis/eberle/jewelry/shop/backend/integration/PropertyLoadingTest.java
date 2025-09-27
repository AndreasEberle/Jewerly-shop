package andreas.kafkis.eberle.jewelry.shop.backend.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Test to demonstrate property loading from different profiles
 */
@SpringBootTest
@ActiveProfiles("test") // This will load application-test.properties
class PropertyLoadingTest {

    @Value("${email.from:}")
    private String fromEmail;

    @Value("${email.test-to:}")
    private String testEmailTo;

    @Value("${storage.type:}")
    private String storageType;

    @Value("${spring.mail.host:}")
    private String mailHost;

    @Test
    void testPropertiesLoadedFromTemplate() {
        System.out.println("=== Property Loading Test ===");
        System.out.println("From Email: " + fromEmail);
        System.out.println("Test Email To: " + testEmailTo);
        System.out.println("Storage Type: " + storageType);
        System.out.println("Mail Host: " + mailHost);
        
        // These should match the values from application-test.properties (template)
        assertEquals("test@jewelryshop.com", fromEmail);
        assertEquals("test@example.com", testEmailTo);
        assertEquals("local", storageType);
        assertEquals("localhost", mailHost);
        
        System.out.println("✅ Properties loaded from application-test.properties (template)");
    }

    @Test
    void testPropertyLoadingExplanation() {
        System.out.println("=== Property Loading Explanation ===");
        System.out.println();
        System.out.println("1. Template file (pushed to git):");
        System.out.println("   → src/test/resources/application-test.properties");
        System.out.println("   → Contains placeholder values like 'your-test-email@gmail.com'");
        System.out.println();
        System.out.println("2. Real values file (NOT pushed to git):");
        System.out.println("   → src/test/resources/application-test-real.properties");
        System.out.println("   → Contains your actual credentials");
        System.out.println("   → Added to .gitignore for security");
        System.out.println();
        System.out.println("3. To use real values in tests:");
        System.out.println("   → Change @ActiveProfiles(\"test\") to @ActiveProfiles(\"test-real\")");
        System.out.println("   → Or copy application-test-real.properties to application-test.properties");
        System.out.println();
        System.out.println("4. Current test uses: @ActiveProfiles(\"test\")");
        System.out.println("   → Loads from application-test.properties (template)");
        System.out.println("   → Values: " + fromEmail + ", " + testEmailTo + ", " + storageType);
    }
}
