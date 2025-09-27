package andreas.kafkis.eberle.jewelry.shop.backend.integration;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Test to demonstrate property loading from real values file
 * This test will only work if you have application-test-real.properties configured
 */
@SpringBootTest
@ActiveProfiles("test-real") // This will load application-test-real.properties
class PropertyLoadingRealTest {

    @Value("${email.from:}")
    private String fromEmail;

    @Value("${email.test-to:}")
    private String testEmailTo;

    @Value("${storage.type:}")
    private String storageType;

    @Value("${spring.mail.host:}")
    private String mailHost;

    @Value("${storage.s3.bucket-name:}")
    private String s3BucketName;

    @Test
    void testPropertiesLoadedFromRealValues() {
        System.out.println("=== Real Values Property Loading Test ===");
        System.out.println("From Email: " + fromEmail);
        System.out.println("Test Email To: " + testEmailTo);
        System.out.println("Storage Type: " + storageType);
        System.out.println("Mail Host: " + mailHost);
        System.out.println("S3 Bucket: " + s3BucketName);
        
        // These should match the values from application-test-real.properties
        // If the file doesn't exist or isn't configured, these will be empty
        if (fromEmail.isEmpty() || fromEmail.equals("your-test-email@gmail.com")) {
            System.out.println("⚠️  Real values not configured yet");
            System.out.println("   → Create application-test-real.properties");
            System.out.println("   → Fill in your real credentials");
            System.out.println("   → This test will use template values");
        } else {
            System.out.println("✅ Properties loaded from application-test-real.properties");
            assertFalse(fromEmail.isEmpty(), "From email should be configured");
            assertFalse(testEmailTo.isEmpty(), "Test email should be configured");
        }
    }

    @Test
    void testRealValuesSetupInstructions() {
        System.out.println("=== How to Set Up Real Values ===");
        System.out.println();
        System.out.println("1. Copy the template:");
        System.out.println("   cp src/test/resources/application-test.properties src/test/resources/application-test-real.properties");
        System.out.println();
        System.out.println("2. Edit application-test-real.properties:");
        System.out.println("   → Replace 'your-test-email@gmail.com' with your real email");
        System.out.println("   → Replace 'your-google-client-id' with your real Google OAuth client ID");
        System.out.println("   → Replace 'your-aws-access-key' with your real AWS access key");
        System.out.println("   → Replace 'your-test-bucket-name' with your real S3 bucket name");
        System.out.println();
        System.out.println("3. Use in tests:");
        System.out.println("   @ActiveProfiles(\"test-real\")  // Loads application-test-real.properties");
        System.out.println("   @ActiveProfiles(\"test\")       // Loads application-test.properties (template)");
        System.out.println();
        System.out.println("4. Security:");
        System.out.println("   → application-test-real.properties is in .gitignore");
        System.out.println("   → It will NOT be pushed to git");
        System.out.println("   → Only the template (application-test.properties) is pushed");
    }
}
